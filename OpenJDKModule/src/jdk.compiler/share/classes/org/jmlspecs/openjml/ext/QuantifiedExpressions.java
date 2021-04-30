/*
 * This file is part of the OpenJML project. 
 * Author: David R. Cok
 */
package org.jmlspecs.openjml.ext;

import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.parser.Tokens.TokenKind.COLON;
import static com.sun.tools.javac.parser.Tokens.TokenKind.COMMA;
import static com.sun.tools.javac.parser.Tokens.TokenKind.RPAREN;
import static com.sun.tools.javac.parser.Tokens.TokenKind.SEMI;

import org.jmlspecs.openjml.IJmlClauseKind;
import org.jmlspecs.openjml.JmlExtension;
import org.jmlspecs.openjml.JmlTree.JmlQuantifiedExpr;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.JmlAttr;
import com.sun.tools.javac.comp.JmlMemberEnter;
import com.sun.tools.javac.parser.JmlParser;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

public class QuantifiedExpressions extends JmlExtension {

    public static class QuantifiedExpression extends IJmlClauseKind.ExpressionKind {
        public QuantifiedExpression(String keyword) { super(keyword); }

        @Override
        public JCExpression parse(JCModifiers mods, String keyword,
                IJmlClauseKind clauseType, JmlParser parser) {
            init(parser);
            int pos = parser.pos();
            parser.nextToken();
            mods = parser.modifiersOpt();
            JCExpression t = parser.parseType();
            if (t.getTag() == JCTree.Tag.ERRONEOUS) return t;
            if (mods.pos == -1) {
                mods.pos = t.pos; // set the beginning of the modifiers
                parser.storeEnd(mods,t.pos);
            }
                                                  // modifiers
            // to the beginning of the type, if there
            // are no modifiers
            ListBuffer<JCVariableDecl> decls = new ListBuffer<JCVariableDecl>();
            int idpos = parser.pos();
            Name id = parser.ident(); // FIXME JML allows dimensions after the ident
            decls.append(toP(parser.maker().at(idpos).VarDef(mods, id, t, null)));
            while (parser.token().kind == COMMA) {
                parser.nextToken();
                idpos = parser.pos();
                id = parser.ident(); // FIXME JML allows dimensions after the ident
                decls.append(toP(parser.maker().at(idpos).VarDef(mods, id, t, null)));
            }
            if (parser.token().kind != SEMI) {
                error(parser.pos(), parser.endPos(), "jml.expected.semicolon.quantified");
                int p = parser.pos();
                parser.skipThroughRightParen();
                return toP(parser.maker().at(p).Erroneous());
            }
            parser.nextToken();
            JCExpression range = null;
            JCExpression pred = null;
            if (parser.token().kind == SEMI) {
                // type id ; ; predicate
                // two consecutive semicolons is allowed, and means the
                // range is null - continue
                parser.nextToken();
                pred = parser.parseExpression();
            } else {
                range = parser.parseExpression();
                if (parser.token().kind == SEMI) {
                    // type id ; range ; predicate
                    parser.nextToken();
                    pred = parser.parseExpression();
                } else if (parser.token().kind == RPAREN || parser.token().kind == COLON) {
                    // type id ; predicate
                    pred = range;
                    range = null;
                } else {
                    error(parser.pos(), parser.endPos(),
                            "jml.expected.semicolon.quantified");
                    int p = parser.pos();
                    parser.skipThroughRightParen();
                    return toP(parser.maker().at(p).Erroneous());
                }
            }
            List<JCExpression> triggers = null;
            if (parser.token().kind == COLON) {
                parser.accept(COLON);
                // triggers
                if (parser.token().kind != RPAREN) {
                    triggers = parser.parseExpressionList();
                }
            }
            JmlQuantifiedExpr q = toP(parser.maker().at(pos).JmlQuantifiedExpr(this, decls.toList(),
                    range, pred));
            q.triggers = triggers;
            return parser.primaryTrailers(q, null); // FIXME - was primarySuffix
        }

        @Override
        public Type typecheck(JmlAttr attr, JCTree tree, Env<AttrContext> env) {
            syms = attr.syms;
            JmlQuantifiedExpr that = (JmlQuantifiedExpr)tree;
            Env<AttrContext> localEnv = attr.envForExpr(that,env);
            
            boolean b = ((JmlMemberEnter)attr.memberEnter).setInJml(true);
            for (JCVariableDecl decl: that.decls) {
                JCModifiers mods = decl.getModifiers();
                if (attr.utils.hasOnly(mods,0)!=0) log.error(mods.pos,"jml.no.java.mods.allowed","quantified expression");
                attr.attribAnnotationTypes(mods.annotations,env);
                attr.allAllowed(mods.annotations, attr.typeModifiers, "quantified expression");
                attr.utils.setExprLocal(mods);
//                if (utils.hasAny(mods,Flags.STATIC)) {
//                    log.error(that.pos,
//                            "mod.not.allowed.here", asFlagSet(Flags.STATIC));
//                }
//                //if (Resolve.isStatic(env)) mods.flags |= Flags.STATIC;  // FIXME - this is needed for variables declared in quantified expressions in invariants - will need to ignore this when pretty printing?
                attr.memberEnter.memberEnter(decl, localEnv);
                decl.type = decl.vartype.type; // FIXME not sure this is needed
            }
            ((JmlMemberEnter)attr.memberEnter).setInJml(b);
            attr.quantifiedExprs.add(that);
            
            if (that.triggers != null && that.triggers.size() > 0) {
            	if (that.kind != qforallKind && that.kind != qexistsKind ) {
                    utils.warning(that.triggers.get(0),"jml.message","Triggers only recognized in \\forall or \\exists quantified expressions");
                    that.triggers = null;
            	} else {
            		for (var t: that.triggers) t.type = attr.attribExpr(t, localEnv, Type.noType);
                    // FIXME - need to check well-formedness of triggers
            	}
            }
            Type resultType = syms.errType;
            try {
                
                if (that.range != null) {
                	that.range.type = attr.attribExpr(that.range, localEnv, syms.booleanType);
                }

                Type t;
                switch (this.name()) {
                    case qexistsID:
                    case qforallID:
                        t = attr.attribExpr(that.value, localEnv, syms.booleanType);
                        resultType = syms.booleanType;
                        break;

                    case qnumofID:
                        t = attr.attribExpr(that.value, localEnv, syms.booleanType);
                        resultType = com.sun.tools.javac.code.JmlTypes.instance(context).BIGINT;
                        break;

                    case qmaxID:
                    case qminID:
                        t = attr.attribExpr(that.value, localEnv, Type.noType);
                        resultType = that.value.type;
                        // FIXME - allow this for any Comparable type
                        //                if (!types.unboxedTypeOrType(resultType).isNumeric()) {
                        //                    log.error(that.value,"jml.internal", "The value expression of a sum or product expression must be a numeric type, not " + resultType.toString());
                        //                    resultType = types.createErrorType(resultType);
                        //                }
                        break;

                    case qsumID:
                    case qproductID:
                        t = attr.attribExpr(that.value, localEnv, Type.noType);
                        resultType = that.value.type;
                        if (!attr.jmltypes.isNumeric(attr.jmltypes.unboxedTypeOrType(resultType))) {
                            error(that.value,"jml.bad.quantifer.expression", resultType.toString());
                            resultType = attr.jmltypes.createErrorType(resultType);
                        }
                        break;

                    default:
                        error(that,"jml.unknown.construct", this.name(),"JmlAttr.visitJmlQuantifiedExpr");
                        break;
                }
                resultType = attr.check(that, resultType, KindSelector.VAL, attr.resultInfo);

                if (attr.utils.rac) {
                    Type saved = resultType;
                    try {
                        if (that.racexpr == null) attr.createRacExpr(that,localEnv,resultType);
                    } finally {
                        resultType = saved;
                    }
                }
            } finally {
                attr.quantifiedExprs.remove(attr.quantifiedExprs.size()-1);
                localEnv.info.scope().leave();
            }
            if (that.range != null && that.range.type.isErroneous()) resultType = that.type = that.range.type;
            return resultType;
        }

    };

    public static final String qforallID = "\\forall";
    public static final IJmlClauseKind qforallKind = new QuantifiedExpression(qforallID);
    public static final String qexistsID = "\\exists";
    public static final IJmlClauseKind qexistsKind = new QuantifiedExpression(qexistsID);
    public static final String qnumofID = "\\num_of";
    public static final IJmlClauseKind qnumofKind = new QuantifiedExpression(qnumofID);
    public static final String qsumID = "\\sum";
    public static final IJmlClauseKind qsumKind = new QuantifiedExpression(qsumID);
    public static final String qproductID = "\\product";
    public static final IJmlClauseKind qproductKind = new QuantifiedExpression(qproductID);
    // maxKind is not final because there are two uses of \max -- MiscExpressions disambiguates
    // but we can't have both of them being registered
    public static final String qmaxID = "\\max";
    public static       IJmlClauseKind qmaxKind = new QuantifiedExpression(qmaxID);
    public static final String qminID = "\\min";
    public static final IJmlClauseKind qminKind = new QuantifiedExpression(qminID);
    public static final String letID = "\\let";
    public static final IJmlClauseKind letKind = new QuantifiedExpression(letID) {
        
        public JCExpression parse(JCModifiers mods, String keyword,
                IJmlClauseKind clauseType, JmlParser parser) {
            init(parser);
            ListBuffer<JCTree.JCStatement> vdefs = new ListBuffer<>();
            int pos = parser.pos();
            parser.nextToken(); // advance over keyword
            if (mods == null) {
            	mods = parser.jmlF.Modifiers(0L);
            }
            utils.setJML(mods);
            do {
                int declpos = parser.pos();
                JCModifiers xmods = parser.jmlF.Modifiers(mods.flags);
                xmods.pos = declpos;
                parser.storeEnd(xmods,declpos);
                JCExpression type = parser.parseType();
                int p = parser.pos();
                Name name = parser.ident();
                JCVariableDecl decl = parser.variableDeclaratorRest(p,xmods,type,name,true,null,true,false);
                decl.pos = p;
                if (decl.init == null) toP(decl);
                vdefs.add(decl);
                if (parser.token().kind != COMMA) break;
                parser.accept(COMMA);
            } while (true);
            parser.accept(SEMI); // FIXME - use wrapup
            JCExpression expr = parser.parseExpression();
            return toP(parser.jmlF.at(pos).LetExpr(vdefs.toList(),expr));
        }
    };
}

