package jburg.burg;

import java.io.*;
import jburg.parser.*;

import antlr.Token;
import antlr.TokenStreamSelector;

/**
 *  This TokenStream implementation performs macro substitution
 *  as it lexes the input.
 *  Macros at the JBurg syntax level are id="new token stream" type,
 *  the new token stream is re-lexed and injected into the token stream.
 *  Macros at the block level are { "regex"="substitution text" } type,
 *  they're handled as straight text substituions.
 */
public class MacroProcessingStream implements antlr.TokenStream, JBurgTokenTypes
{
    JBurgANTLRLexer impl;

    public MacroProcessingStream(File in)
        throws Exception
        {
            impl = new JBurgANTLRLexer(new FileInputStream(in));

            impl.mainSourceFile = in;

            JBurgANTLRLexer.selector =  new TokenStreamSelector();
            JBurgANTLRLexer.selector.addInputStream(impl, "main");
            JBurgANTLRLexer.selector.select("main");
        }

    @SuppressWarnings("deprecation")
	public Token nextToken()
        throws antlr.TokenStreamException
        {
            Token result = null;

            do 
            {
                try
                {
                    result = JBurgANTLRLexer.selector.nextToken();

                    //  Check for a substitution.
                    switch ( result.getType() )
                    {
                        case IDENTIFIER:
                            {
                                //  An identifier at the JBurg syntax level can be replaced 
                                //  by a string of JBurg tokens, which are relexed.
                                java.util.Map<String,String> subs = impl.getCurrent().jburgSubstitutionText;
                                String id = result.getText();

                                if ( subs.containsKey(id ) )
                                {
                                    result = null;  //  Discard the substituted token.
                                    impl.pushMacroLexer(new JBurgANTLRLexer(new StringBufferInputStream(subs.get(id))));
                                }
                            }
                        case BLOCK:
                            {
                                //  A regex within a block can be replaced by substitution text.
                                //  The substitution text isn't relexed by JBurg, since the block
                                //  as a whole isn't being lexed in any meaningful way.
                                java.util.Map<String,String> subs = impl.getCurrent().blockSubstitutionText;
                                if ( !subs.isEmpty() )
                                {
                                    String block_body = result.getText();

                                    for ( String key: subs.keySet() )
                                    {
                                        block_body = block_body.replaceAll(key, subs.get(key));
                                    }

                                    result.setText(block_body);
                                }
                            }
                    }
                }
                catch ( antlr.TokenStreamRetryException retry )
                {
                    continue;
                }

            } while ( result == null );

            return result;
        }
}
