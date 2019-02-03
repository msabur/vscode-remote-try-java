package org.jmlspecs.openjml.ext;

import static com.sun.tools.javac.parser.Tokens.TokenKind.SEMI;
import static org.jmlspecs.openjml.JmlTokenKind.ENDJMLCOMMENT;

import org.jmlspecs.openjml.IJmlClauseType;
import org.jmlspecs.openjml.JmlExtension;
import org.jmlspecs.openjml.JmlTree.JmlMethodClause;
import org.jmlspecs.openjml.JmlTree.JmlMethodClauseCallable;
import org.jmlspecs.openjml.JmlTree.JmlMethodClauseExpr;
import org.jmlspecs.openjml.JmlTree.JmlMethodSig;
import org.jmlspecs.openjml.JmlTree.JmlStoreRefKeyword;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.JmlAttr;
import com.sun.tools.javac.parser.JmlParser;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

public class CallableClauseExtension extends JmlExtension.MethodClause {

    public static final String callableID = "callable";
    
    @Override
    public IJmlClauseType[]  clauseTypes() { return new IJmlClauseType[]{
            callableClause }; }
    
    public static final IJmlClauseType callableClause = new IJmlClauseType.MethodClause() {
        @Override
        public String name() { return callableID; }
        @Override
        public boolean oldNoLabelAllowed() { return false; }
        @Override
        public boolean preOrOldWithLabelAllowed() { return false; }

        @Override
        public JmlMethodClauseCallable parse(JCModifiers mods, String keyword, IJmlClauseType clauseType, JmlParser parser) {
            int pp = parser.pos();
            int pe = parser.endPos();
            init(parser);
            
            parser.warnNotImplemented(pp, keyword, "JmlParser");
            scanner.setJmlKeyword(false);
            parser.nextToken();

            JmlStoreRefKeyword refkeyword = parser.parseOptStoreRefKeyword();
            List<JmlMethodSig> sigs = null;
            if (refkeyword == null) {
                sigs = parser.parseMethodNameList();
            }
            scanner.setJmlKeyword(true);
            int endpos = parser.pos();
            parser.accept(SEMI);
            JmlMethodClauseCallable ec;
            if (refkeyword != null) {
                ec = toP(jmlF.at(pp).JmlMethodClauseCallable(
                        refkeyword));
            } else {
                ec = toP(jmlF.at(pp).JmlMethodClauseCallable(sigs));
            }
            return ec;
        }
        
        @Override
        public Type typecheck(JmlAttr attr, JCTree expr, Env<AttrContext> env) {
            // TODO Auto-generated method stub
            return null;
        }
};

    
 }
