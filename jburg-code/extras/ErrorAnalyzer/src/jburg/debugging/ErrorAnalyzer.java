package jburg.debugging;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * The ErrorAnalyzer parses JBurg diagnostic
 * dumps and presents them in a simple UI.
 * 
 * @author tharwood@adobe.com
 */
@SuppressWarnings({"unused","nls"})
public class ErrorAnalyzer
{
    JFrame mainFrame;
    ErrorTree  labelTree;
    JScrollPane scroller;
    JLabel statusBar;
    
    String backingFileName;

    Set<String> allGoals = new HashSet<String>();

    Map<Integer, List<SubgoalRecord>> subgoalsByRule = new HashMap<Integer, List<SubgoalRecord>>();
    
    public static void main(String[] args) 
    {
        new ErrorAnalyzer(args);
    }

    public static void usage(java.io.PrintStream out)
    {
        out.println("Usage: jburg.debugging.ErrorAnalyzer <xml file>"); //$NON-NLS-1$
    }
    
    private ErrorAnalyzer(String[] args)
    {
        createUI();
        
        for ( int i = 0; i < args.length; i++ )
        {
            if ( "-tokenTypes".equalsIgnoreCase(args[i]) )
            {
                if ( i+1 < args.length)
                {
                    i++;
                    loadOpcodes(args[i]);
                }
                else
                {
                    usage(System.err);
                }
            }
            else if ( "--help".equalsIgnoreCase(args[i]) || "-h".equals(args[i]))
            {
                usage(System.out);
            }
            else
            {
                loadFile(args[i]);
            }
        }
        
        if ( this.backingFileName == null )
            chooseAndLoadFile();
    }
    
    void parse()
    {   
        try
        {
            new ErrorParser().parse(this.backingFileName);
            this.scroller = new JScrollPane(labelTree);
            mainFrame.add(scroller);
        } catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
    
    private void loadFile(String file_name)
    {
        this.backingFileName = file_name;
        
        if ( scroller != null )
            mainFrame.remove(scroller);

        scroller = null;
        labelTree = null;
        parse();
        mainFrame.setVisible(true);
        setStatus("");
        
    }
    
    private void createUI()
    {
        mainFrame = new JFrame("JBurg ErrorAnalyzer");
        mainFrame.resize(1200,850);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //  Set up the File menu.
        JMenu file_menu = new JMenu("File");
        file_menu.setMnemonic(KeyEvent.VK_F);
        
        file_menu.add(addMenuItem(FILE_OPEN, KeyEvent.VK_O, ENABLE_MENU_ITEM, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
                new ActionListener() {             
            public void actionPerformed(ActionEvent e) {
                chooseAndLoadFile();
            }
        })
        );
        
        file_menu.add(addMenuItem(FILE_REFRESH, KeyEvent.VK_R, ENABLE_MENU_ITEM, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
                new ActionListener() {             
            public void actionPerformed(ActionEvent e) {
                loadFile(backingFileName);
            }
        })
        );
        
        file_menu.add(addMenuItem(FILE_EXIT, KeyEvent.VK_X, ENABLE_MENU_ITEM, new ActionListener() { public void actionPerformed(ActionEvent e) {System.exit(0);}} ));
        
        //  Set up the Edit menu.
        JMenu edit_menu = new JMenu("Edit");
        edit_menu.add(addMenuItem(EDIT_FIND_NEXT, KeyEvent.VK_F, ENABLE_MENU_ITEM, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
                new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                labelTree.findNextUnlabeled();
            }

        })
        );

        edit_menu.add(addMenuItem(EDIT_FIND_MISSING, KeyEvent.VK_M, ENABLE_MENU_ITEM, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
                new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                Object[] choices = allGoals.toArray();
                Object selection = JOptionPane.showInputDialog(null,
                    "Select missing goal", "Input",
                    JOptionPane.INFORMATION_MESSAGE, null,
                    choices, choices[0]
                );

                if ( selection != null )
                    labelTree.findMissingLabel(selection.toString());
            }

        })
        ) ;

        edit_menu.add(addMenuItem(EDIT_SHOW_REDUCTIONS, KeyEvent.VK_S, ENABLE_MENU_ITEM, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    String[] choices = labelTree.getCurrentNode().getGoals();
                    Object selection = JOptionPane.showInputDialog(null,
                        "Select goal state", "Input",
                        JOptionPane.INFORMATION_MESSAGE, null,
                        choices, choices[0]
                    );

                    if ( selection != null )
                    {
                        JTextArea display = new JTextArea();
                        labelTree.showReductions(display, selection.toString());
                   
                        JFrame f = new JFrame("Reductions");
                        new CloseListener(f);
                        f.setLayout(new BorderLayout());
                        f.add(display, BorderLayout.CENTER);
                        f.add(new JLabel("                              "), BorderLayout.SOUTH);
                        f.pack();
                        f.setVisible(true);
                    }
                }
            })
        );
        
        JMenuBar menu_bar  = new JMenuBar();
        menu_bar.add(file_menu);
        menu_bar.add(edit_menu);
    
        mainFrame.setJMenuBar(menu_bar);
        
        //  Add the status bar.
        JPanel status_area = new JPanel(new BorderLayout());
        status_area.setBorder(new EtchedBorder());
        mainFrame.add(status_area, BorderLayout.SOUTH);
        
        this.statusBar = new JLabel(" ");
        status_area.add(this.statusBar, BorderLayout.WEST);
    }
    
    void setStatus(String status) {
        this.statusBar.setText(status);
    }
    
    /**
     * Display a GUI file chooser, and load the selected file.
     */
    private void chooseAndLoadFile()
    {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(mainFrame);

        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            try
            {
                loadFile(fc.getSelectedFile().getCanonicalPath());
            }
            catch ( Exception ex )
            {
                setStatus(ex.getMessage());
            }
        } 
    }
    
    static final Map<String,Boolean> createNode = new HashMap<String,Boolean>();
    static final Map<String,Boolean> mustBeFeasible = new HashMap<String, Boolean>();

    static
    {
        createNode.put("BurmDump",true);
        createNode.put("goal",true);
        createNode.put("subgoals",false);
        createNode.put("subgoal",false);
        createNode.put("accessPath",false);

        mustBeFeasible.put("goal",true);
    }

    static boolean needsNode(String name, Attributes attributes)
    {
        return
            ((! createNode.containsKey(name) ) || createNode.get(name)) &&
            ((! mustBeFeasible.containsKey(name) ) || getIntAttribute(attributes, "cost") < Integer.MAX_VALUE)
            ;
    }
    
    /**
     * Parse a JBurg diagnostic dump into its ErrorTree representation.
     */
    class ErrorParser extends org.xml.sax.ext.DefaultHandler2
    {
        Stack<DiagnosticNode> nodeStack = new Stack<DiagnosticNode>();
        
        StringBuffer elementChars = null; 

        List<SubgoalRecord> subgoals = null;
        SubgoalRecord subgoal = null;

        ErrorParser()
        {
        }

        void parse(String filename)
        throws Exception
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
    
            SAXParser parser = factory.newSAXParser();
            java.io.InputStream input = new ProgressMonitorInputStream(mainFrame, "Parsing " + filename, new java.io.FileInputStream(filename) ); //$NON-NLS-1$
            parser.parse(input, this);
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
        {   
            boolean createNode = needsNode(localName, attributes);

            DiagnosticNode next =  createNode ?
                new DiagnosticNode(localName, attributes):
                null;

            if ( "BurmDump".equals(localName))
            {
                labelTree = new ErrorTree(next);   
            }
            else if ( "goal".equals(localName) && createNode )
            {
                assert (nodeStack.peek().getLabel().equals("node"));
                nodeStack.peek().addGoal(next);
                allGoals.add(next.properties.get("name"));
            }
            else if ( "subgoals".equals(localName) )
            {
                this.subgoals = new ArrayList<SubgoalRecord>();
                subgoalsByRule.put(getIntAttribute(attributes, "rule"), this.subgoals);
            }
            else if ( "subgoal".equals(localName) )
            {
                this.subgoals.add(new SubgoalRecord(attributes));
            }
            else if ( "accessPath".equals(localName) )
            {
                this.subgoals.get(this.subgoals.size()-1).accessPath.add(getIntAttribute(attributes,"index"));
            }
            else if ( "Exception".equals(localName) || "AST".equals(localName) )
            {
                this.elementChars = new StringBuffer();
            }
            
            if ( needsNode(localName, attributes) )
            {
                if ( !nodeStack.isEmpty() )
                    nodeStack.peek().add(next);
            }
                
            this.nodeStack.push(next);
        }
        
        @Override
        public void endElement(String uri,  String localName,  String qName)
        {
            if ( this.elementChars != null )
            {
                this.nodeStack.peek().add(new DiagnosticNode(this.elementChars.toString(), new org.xml.sax.helpers.AttributesImpl()));
                this.elementChars  = null;
            }

            this.nodeStack.pop();
        }
        
        @Override
        public void characters(char[] ch,
                int start,
                int length)
         throws SAXException
         {
            if ( this.elementChars != null )
            {
                this.elementChars.append(ch, start, length);
            }
         }
    }
    
    
    @SuppressWarnings("serial")
    class ErrorTree extends JTree implements TreeSelectionListener
    {
        /** Selected subtree in preorder. */
        Enumeration<DiagnosticNode> treeInPreorder = null;
        
        /** Children of selected node */
        Enumeration<DiagnosticNode> usualSuspects = null;
        
        /** 
         *  Set during internal repositioning operations
         *  to suppress state reset operations that are
         *  done in response to user initiated selection. 
         */
        boolean internalReposition = false;
        
        DiagnosticNode rootNode;
        
        ErrorTree(DiagnosticNode root)
        {
            super(root);
            this.rootNode = root;
            
            this.addTreeSelectionListener(this);
        }
        
        /**
         *  Find the next child of the selected 
         *  node that is not labeled with the
         *  desired goal state.
         */
        @SuppressWarnings("unchecked")
        public void findMissingLabel(String desiredState)
        {
            if ( desiredState == null || desiredState.equals("") )
                return;

            if ( usualSuspects == null)
                usualSuspects = getCurrentNode().children();
            
            while ( usualSuspects.hasMoreElements() )
            {
                DiagnosticNode n = usualSuspects.nextElement();
                if ( n.label.equals("node") && ! n.hasGoal(desiredState) )
                {
                    reposition(n);
                    return;
                }
            }
        }

        /**
         *  "Play reducer" and show the reductions that would
         *  reduce the selected node to the desired goal state.
         */
        @SuppressWarnings("unchecked")
        public void showReductions(JTextArea display, String goalState)
        {
            if ( goalState == null || goalState.equals("") )
                return;

            showReductions(getCurrentNode(), goalState, display, 0);
        }

        private void showReductions(DiagnosticNode current, String goalName, JTextArea display, int level)
        {
            if ( level > 0 )
                display.append(String.format("%"+level*2+"s", " "));
            DiagnosticNode goal = current.findGoal(goalName);

            if ( goal == null )
            {
                display.append(String.format("%s doesn't satisfy %s\n", current, goalName));
                return;
            }

            Integer ruleNum = goal.getIntProperty("rule");

            if ( subgoalsByRule.containsKey(ruleNum) )
            {
                List<SubgoalRecord> subgoals = subgoalsByRule.get(ruleNum);

                for ( int i = 0; i < subgoals.size(); i++ )
                {
                    SubgoalRecord subgoal = subgoals.get(i);
                    DiagnosticNode next = current;
                    for ( Integer ap: subgoal.accessPath )
                    {
                        System.out.flush();
                        next = next.getNodeAt(ap);
                    }
                    showReductions(next, subgoal.goal, display, level+1);
                }
            }

            display.append(String.format("reduce %s to %s by rule #%s\n", current, goalName, ruleNum));
        }

        /**
         * Get the node under the mouse.
         * @param e - the mouse event that triggered this action.
         * @param set_selection - if true, also set the node as selected.
         * @return the DiagnosticNode under the mouse, or null if the mouse
         *   is in some random location.
         */
        private DiagnosticNode getNode(MouseEvent e, boolean set_selection)
        {
            TreePath selPath = getPathForLocation(e.getX(), e.getY());
            
            if ( null == selPath )
            {
                // Random bogus mouse event.
                return null;
            }
            
            if ( set_selection )
                this.setSelectionPath(selPath);
            
            return (DiagnosticNode)selPath.getLastPathComponent();
        }
        
        /**
         *  Get the currently selected node.
         *  @return the last component in the selected tree path.
         */
        private DiagnosticNode getCurrentNode()
        {
            Object current = this.getLastSelectedPathComponent();
            if ( current != null )
                return (DiagnosticNode)current;
            else
                return this.rootNode;
        }
        
        /**
         *   Find the next-in-preorder node that is not labeled.
         *   @note Not all unlabeled nodes are errors; some
         *   nodes are part of a tree path to a subgoal and
         *   are only inspected by the pattern matcher.
         */
        @SuppressWarnings({ "unchecked" })
        void findNextUnlabeled()
        {
            if ( null == this.treeInPreorder )
            {
                DiagnosticNode n = getCurrentNode();
                treeInPreorder = n.preorderEnumeration();
            }
            
            boolean found_unlabeled = false;
            while ( !found_unlabeled && treeInPreorder.hasMoreElements() )
            {
                DiagnosticNode n = treeInPreorder.nextElement();
                
                if ( "node".equals(n.getLabel()) && !n.hasGoal() )
                {
                    reposition(n);
                    found_unlabeled = true;
                }
            }
            
            if ( found_unlabeled )
                setStatus("");
            else
                setStatus("No unlabeled node found.");
        }
        
        /**
         *  Reposition the display to the specified node,
         *  typically due to a find operation.
         *  @param new_pos - the node to reposition to.
         *  @see internalReposition instance flag, which
         *    inhibits state reset actions that would be
         *    taken in response to node selection by the user. 
         */
        private void reposition(DiagnosticNode new_pos)
        {
            boolean prev_reposition_state = this.internalReposition;
            try
            {
                this.internalReposition = true;
                TreePath path = new TreePath(new_pos.getPath());
                this.setSelectionPath(path);
                this.scrollPathToVisible(path);
                this.expandPath(path);
            }
            finally
            {
                this.internalReposition = prev_reposition_state;
            }
        }

        public void valueChanged(TreeSelectionEvent e)
        {
            if ( !this.internalReposition )
            {
                //  Reset the traversals.
                treeInPreorder = null;
                usualSuspects = null;
            }
        }
    }
    /**
     * A tree node with knowledge of its goal states and some formatting.
     */
    @SuppressWarnings("serial")
    class DiagnosticNode extends DefaultMutableTreeNode
    {   
        final String label;
        Map<String,String> properties;
        Vector<DiagnosticNode> goals;
        
        DiagnosticNode(String label, Attributes attributes)
        {   
            this.label = label;
            this.properties = new HashMap<String,String>();
            decodeAttributes(attributes);
        }
        
        /**
         * Decode the XML record's attributes into
         * a properties map, and store a string
         * representation in the node's user object
         * for display.
         */
        void decodeAttributes(Attributes attributes)
        {
            StringBuffer buffer = new StringBuffer();
            
            buffer.append(label);
            
            for ( int index = 0; index < attributes.getLength(); index++)
            {
                String attr_name = attributes.getLocalName(index);
                String attr_value = attributes.getValue(index);
                
                properties.put(attr_name, attr_value);
                
                buffer.append(" ");
                buffer.append(attr_name);
                buffer.append("=");

                if ( "operator".equals(attr_name) )
                {
                    if ( !opcodeToOpcodeName.isEmpty() )
                        buffer.append(decodeTokenType(Integer.parseInt(attr_value)));
                    else
                        buffer.append(attr_value);
                }
                else
                {
                    try
                    {
                        buffer.append(java.net.URLDecoder.decode(attr_value,"UTF-8"));
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        // Can't happen, compile appeasement
                        e.printStackTrace();
                    }
                }
            }
            
            super.setUserObject(buffer.toString());
        }
        
        void addGoal(DiagnosticNode goalState)
        {
            if ( null == this.goals)
                this.goals = new Vector<DiagnosticNode>();
            this.goals.add(goalState);
        }
        
        boolean hasGoal()
        {
            return this.goals != null && this.goals.size() > 0;
        }

        DiagnosticNode findGoal(String goalName)
        {
            if ( this.goals != null )
            {
                for ( DiagnosticNode n: this.goals )
                {
                    if ( goalName.equals(n.properties.get("name")) )
                        return n;
                }
            }

            return null;
        }
        
        boolean hasGoal(String goalName)
        {
            return findGoal(goalName) != null;
        }
        
        String getLabel()
        { 
            return this.label;
        }

        int getIntProperty(String name)
        {
            return Integer.parseInt(this.properties.get(name));
        }

        DiagnosticNode getNodeAt(int idx)
        {
            int lastFound = 0;

            for ( int i = 0; i < super.getChildCount(); i++ )
            {
                DiagnosticNode n = (DiagnosticNode)getChildAt(i);
                if (n.label.equals("node"))
                {
                    if ( lastFound == idx )
                        return n;
                    else
                        lastFound++;
                }
            }
            throw new IllegalStateException(String.format("%s has no node[%d]",this,idx));
        }

        /**
         * @return all goal states this node can be reduced to.
         */
        String[] getGoals()
        {
            String[] result = new String[this.goals.size()];
            int idx = 0;
            for ( DiagnosticNode goal: this.goals )
                result[idx++] = goal.properties.get("name");
            return result;
        }
    }
    
    /**
     * Add a menu item with no keyboard accelerator.
     * @param label - descriptive text.
     * @param keyboard_shortcut - the menu keyboard shortcut for this item.
     * @param enabled_state - initial state of the menu item.
     * @param listener - an action listener to perform the menu item's function.
     * @return the new menu item.
     */
    private JMenuItem addMenuItem(String label, int keyboard_shortcut, boolean enabled_state, ActionListener listener)
    {
        return addMenuItem(label, keyboard_shortcut, enabled_state, 0, listener);
    }
    
    /**
     * Add a menu item with a keyboard accelerator.
     * @param label - descriptive text.
     * @param keyboard_shortcut - the menu keyboard shortcut for this item.
     * @param enabled_state - initial state of the menu item.
     * @param accelerator_mask - Key or keys (e.g., ctrl key, Apple key, shift key)
     *   that activate this item outside its menu when pressed in conjunction with its shortcut key. 
     * @param listener - an action listener to perform the menu item's function.
     * @return the new menu item.
     */
    private JMenuItem addMenuItem(String label, int keyboard_shortcut, boolean enabled_state, int accelerator_mask, ActionListener listener) 
    {
        JMenuItem item = new JMenuItem(label, keyboard_shortcut);
        this.menuItems.put(label, item);
        item.setEnabled(enabled_state);
        item.addActionListener(listener);
        
        if ( accelerator_mask != 0 )
            item.setAccelerator(KeyStroke.getKeyStroke(keyboard_shortcut, accelerator_mask, false));
        
        return item;
    }
    
    /**
     * Get a menu item by name.
     * @param key - the menu item's label.
     * @return the corresponding menu item.
     */
    private JMenuItem getMenuItem(String key)
    {
        JMenuItem result = this.menuItems.get(key);
        assert(result != null);
        return result;
    }
    
    Map<String, JMenuItem> menuItems = new HashMap<String,JMenuItem>();
    
    /*
     * Menu choices 
     */
    
    /** File/Exit */
    static final String FILE_EXIT = "Exit";
    /** File/Open */
    static final String FILE_OPEN = "Open...";
    /** File/Refresh */
    static final String FILE_REFRESH = "Refresh";
    /** Edit/FindNext */
    static final String EDIT_FIND_NEXT = "Find next unlabeled";
    /** Edit/FindMissingLabel */
    static final String EDIT_FIND_MISSING = "Find missing label...";
    /** Edit/ShowReductionsFor */
    static final String EDIT_SHOW_REDUCTIONS = "Show reductions...";

    /** Select the node under the mouse. */
    private static final boolean SELECT_ON_MOUSE_EVENT = true;
    /** Don't select the node under the mouse. */
    private static final boolean DONT_SELECT_ON_MOUSE_EVENT = false;
    /** Strip line information from intermediate CallStackLevel nodes */
    private static final boolean STRIP_LINE_INFORMATION = true;
    /** Keep line information from intermediate CallStackLevel nodes */
    private static final boolean KEEP_LINE_INFORMATION = false;

    /** Initial state of a menu item: enabled. */
    private static final boolean ENABLE_MENU_ITEM = true;
    /** Initial state of a menu item: disabled. */
    private static final boolean DISABLE_MENU_ITEM = false;
    

    static public String decodeTokenType(int type) 
    {
        String result = opcodeToOpcodeName.get(type);

        if ( null == result )
            result = Integer.toString(type);
        return result.toString();
    }

    static Map<Integer, String> opcodeToOpcodeName = new HashMap<Integer,String>();
    
    private void loadOpcodes(String class_name)
    {
        try
        {
            Class<? extends Object> tokenTypes = Class.forName(class_name);
            //  Traverse the names of the OP_foo constants
            //  in AbcConstants and load their values.
            for ( java.lang.reflect.Field f: tokenTypes.getFields())
            {
                String field_name = f.getName();
               try
               {
                   int field_value = f.getInt(null);
                   opcodeToOpcodeName.put(field_value, field_name);
               }
               catch ( Exception noFieldValue)
               {
                   //  Ignore, continue...
               }
            }
        }
        catch ( Throwable no_class)
        {
            setStatus("Unable to load " + class_name + ": " + no_class.getLocalizedMessage());
            no_class.printStackTrace();
        }
    }

    /**
     * Fetch an attribute.
     * @param attributes - current XML tag's attributes.
     * @param attr_name - the desired attribute's name.
     * @return the URL-decoded value of the attribute.
     * @throws UnsupportedEncodingException
     */
    static String getStringAttribute(Attributes attributes, String attr_name) 
    {
        try
        {
            String result = java.net.URLDecoder.decode(attributes.getValue(attr_name),"UTF-8");
            return result;
        }
        catch ( UnsupportedEncodingException ex )
        {
            // Doesn't happen unless the attribute wasn't encoded by the BURM dump.
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * Fetch an int attribute.
     * @param attributes - current XML tag's attributes.
     * @param attr_name - the desired attribute's name.
     * @return the int value of the attribute.
     * @throws UnsupportedEncodingException
     */
    static int getIntAttribute(Attributes attributes, String attr_name)
    {
        return Integer.parseInt(getStringAttribute(attributes, attr_name), 10);
    }

    /**
     * Fetch a boolean attribute.
     * @param attributes - current XML tag's attributes.
     * @param attr_name - the desired attribute's name.
     * @return the int value of the attribute.
     * @throws UnsupportedEncodingException
     */
    static boolean getBooleanAttribute(Attributes attributes, String attr_name)
    {
        return Boolean.parseBoolean(getStringAttribute(attributes, attr_name));
    }

    /**
     * The Analyzer's representation of a subgoal.
     */
    static class SubgoalRecord
    {
        SubgoalRecord(Attributes attributes)
        {
            this.goal = getStringAttribute(attributes, "goal");
            this.isNary = getBooleanAttribute(attributes, "nary");
            this.startIndex = getIntAttribute(attributes, "startIndex");
        }

        String goal;
        boolean isNary;
        int startIndex;

        List<Integer> accessPath = new ArrayList<Integer>();
    }

    /**
     * Listen for window-close commands.
     */
    static class CloseListener implements ActionListener
    {
        private JFrame frame;

        CloseListener(JFrame frame)
        {
            this.frame = frame;

            KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false);
            frame.getRootPane().registerKeyboardAction(this, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        }


        /**
         *  Close the frame.
         */
        @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.setVisible(false);
                frame.dispose();
            }
    }
}
