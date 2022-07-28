import java.lang.reflect.Array;
import java.util.*;

public class Grammar
{
    //changes should be made to variable array
    private HashMap<String, ArrayList<ArrayList<String>>> rules;
    private ArrayList<String> variables;
    boolean isChomskyForm;
    boolean isGreibachNormalForm;
    boolean isDeleteTrash;


    public Grammar(HashMap<String, ArrayList<ArrayList<String>>> rules,ArrayList<String> variables)
    {
        this.rules = rules;
        this.variables=variables;
        isGreibachNormalForm = true;
        isChomskyForm = true;
        isDeleteTrash = true;
        checkGrammar();
        deleteTrash();
    }
    void checkGrammar()
    {
        for (String key:rules.keySet())
        {
            ArrayList<ArrayList<String>> grammars = rules.get(key);
            for (ArrayList<String> grammar:grammars)
            {
                //checking isDeleteTrash
                if (grammar.size()==1 && grammar.get(0).contains("<"))
                    isDeleteTrash = false;
                //checking Chomsky Form
                switch (grammar.size())
                {
                    case 1:
                        if (grammar.get(0).contains("<"))
                            isChomskyForm = false;
                        break;
                    case 2:
                        if (!grammar.get(0).contains("<") || !grammar.get(1).contains("<"))
                            isChomskyForm = false;
                        break;
                    default:
                        isChomskyForm = false;
                }
                //checking GreibachNormalForm
                if (grammar.get(0).contains("<"))
                    isGreibachNormalForm = false;
                else
                    for (int k=1;k<grammar.size();k++)
                        if (!grammar.get(k).contains("<"))
                        {
                            isGreibachNormalForm = false;
                            break;
                        }
            }
        }
    }
    //variables should be update
    void deleteTrash()
    {
        //removing unit production
        ArrayList<String[]> unitVariables = findUnitProduction();
        if (unitVariables.size()>0)
            isDeleteTrash=false;
        while (unitVariables.size()>0)
        {
            for (String[] unitVariable : unitVariables)
            {
                for (String key : setToArray(rules.keySet()))
                {
                    ArrayList<ArrayList<String>> grammars = rules.get(key);
                    for (int c = grammars.size() - 1; c >= 0; c--)
                    {
                        ArrayList<String> grammar = grammars.get(c);
                        for (int i = 0; i < grammar.size(); i++)
                        {
                            if (grammar.get(i).equals(unitVariable[0]))
                            {
                                ArrayList<String> cp = cpArray(grammar);
                                cp.set(i, unitVariable[1]);
                                if (!grammars.contains(cp))
                                {
                                    grammars.add(0, cp);
                                    c++;
                                }
                            }
                        }
                    }
                }
            }
            unitVariables=findUnitProduction();
        }
        System.out.println("after removing unit:_______________");
        System.out.println(rules);
        //finding lambda
        ArrayList<String> lambdaVariables = new ArrayList<>();
        for (String key:setToArray(rules.keySet()))
        {
            ArrayList<ArrayList<String>> grammars = rules.get(key);
            for (int c=0;c<grammars.size();c++)
            {
                ArrayList<String> grammar = grammars.get(c);
                if (grammar.size()==1 )
                {
                    if (grammar.get(0).equals("#"))
                    {
                        lambdaVariables.add(key);
                        grammars.remove(grammar);
                        c--;
                    }
                }
                if (grammars.size() == 0)
                    rules.remove(key);
            }
        }
        //removing lambda
        if (lambdaVariables.size()>0)
        {
            isDeleteTrash = false;
            for (String variable:lambdaVariables)
            {
                for (String key:setToArray(rules.keySet()))
                {
                    ArrayList<ArrayList<String>> grammars = rules.get(key);
                    for (int c=0;c<grammars.size();c++)
                    {
                        ArrayList<String> grammar = grammars.get(c);
                        if (grammar.contains(variable))
                        {
                            ArrayList<String> temp = cpArray(grammar);
                            temp.remove(variable);
                            if (temp.size()==0)
                                continue;
                            grammars.add(temp);
                        }
                    }
                }
            }
        }
        System.out.println("after removing lambda_________");
        System.out.println(rules);
        //find variable which won't terminate
        Set<String> v = new HashSet<>();
        int v_size = -1;
        while (v_size != v.size())
        {
            v_size = v.size();
            for (String key:rules.keySet())
            {
                if (v.contains(key))
                    continue;
                ArrayList<ArrayList<String>> grammars = rules.get(key);
                for (ArrayList<String> grammar:grammars)
                {
                    boolean canBeAdded = true;
                    for (int k=0;k<grammar.size();k++)
                    {
                        if (grammar.get(k).contains("<") && !v.contains(grammar.get(k)) )
                        {
                            canBeAdded = false;
                            break;
                        }
                    }
                    if (canBeAdded)
                    {
                        v.add(key);
                        break;
                    }
                }
            }
        }
        //find non reachable variable
        Set<String> reachable = new HashSet<>();
        Stack<String> stack = new Stack<>();
        stack.add(variables.get(0));
        while (!stack.empty())
        {
            reachable.add(stack.peek());
            ArrayList<ArrayList<String>> grammars = rules.get(stack.pop());
            if (grammars == null)
                continue;
            for (ArrayList<String> grammar:grammars)
            {
                for (int k = 0; k<grammar.size();k++)
                {
                    if (grammar.get(k).contains("<"))
                        if (!stack.contains(grammar.get(k)) && !reachable.contains(grammar.get(k)))
                            stack.add(grammar.get(k));
                }
            }
        }
        //remove useless productions
        if (v.size() != variables.size() || reachable.size() != variables.size())
        {
            isDeleteTrash = false;
            for (String key:setToArray(rules.keySet()))
            {
                if (!v.contains(key) || !reachable.contains(key))
                {
                    rules.remove(key);
                    continue;
                }
                ArrayList<ArrayList<String>> grammars = rules.get(key);
                for (int h = 0;h<grammars.size();h++)
                {
                    ArrayList<String> grammar = grammars.get(h);
                    for (int k=0;k<grammar.size();k++)
                    {
                        if (grammar.get(k).contains("<"))
                        {
                            if (!v.contains(grammar.get(k)) || !reachable.contains(grammar.get(k)))
                            {
                                grammars.remove(grammar);
                                h--;
                                break;
                            }
                        }
                    }
                }
                if (grammars.size()==0)
                    rules.remove(key);
            }
        }
        System.out.println("after removing useless_______");
        System.out.println(rules);
    }
    Grammar changeToChomskyForm()
    {
        HashMap<String,ArrayList<ArrayList<String>>> chomskyRules = new HashMap<>();
        HashMap<String,String> terminalsAdded = new HashMap<>();
        ArrayList<String> chomskyVariables = new ArrayList<>();
        ArrayList<String> keys = setToArray(rules.keySet());
        for(int k=0;k<keys.size();k++)
        {
            if (!chomskyVariables.contains(keys.get(k)))
                chomskyVariables.add(keys.get(k));
            ArrayList<ArrayList<String>> grammars = rules.get(keys.get(k));
            for (int c=0;c<grammars.size();c++)
            {
                ArrayList<String> grammar = grammars.get(c);
                if (grammar.size()==1)
                {
                    if (!chomskyRules.containsKey(keys.get(k)))
                    {
                        ArrayList<ArrayList<String>> temp0 = new ArrayList<>();
                        temp0.add(grammar);
                        chomskyRules.put(keys.get(k),temp0);
                    }
                    else
                        chomskyRules.get(keys.get(k)).add(grammar);
                }
                if (grammar.size()>=2)
                {
                    ArrayList<String> chGrammar = cpArray(grammar);
                    for (int z=0;z<chGrammar.size();z++)
                    {
                        if (!chGrammar.get(z).contains("<"))
                        {
                            if (!terminalsAdded.containsKey(chGrammar.get(z)))
                            {
                                ArrayList<ArrayList<String>> temp0 = new ArrayList<>();
                                ArrayList<String> temp1 = new ArrayList<>();
                                temp1.add(chGrammar.get(z));
                                temp0.add(temp1);
                                terminalsAdded.put(chGrammar.get(z),"<T"+terminalsAdded.size()+">");
                                chomskyRules.put("<T"+chomskyVariables.size()+">",temp0);
                                chGrammar.set(z,"<T"+chomskyVariables.size()+">");
                                chomskyVariables.add("<T"+chomskyVariables.size()+">");
                            }
                            else
                                chGrammar.set(z,terminalsAdded.get(chGrammar.get(z)));
                        }
                    }
                    while (chGrammar.size()>2)
                    {
                        ArrayList<ArrayList<String>> temp0 = new ArrayList<>();
                        ArrayList<String> temp1 = new ArrayList<>();
                        temp1.add(chGrammar.get(0));
                        temp1.add(chGrammar.get(1));
                        temp0.add(temp1);
                        chomskyRules.put("<V"+chomskyVariables.size()+">",temp0);
                        chGrammar.remove(0);
                        chGrammar.set(0,"<V"+chomskyVariables.size()+">");
                        chomskyVariables.add("<V"+chomskyVariables.size()+">");
                    }
                    if (chomskyRules.get(keys.get(k))==null)
                    {
                        ArrayList<ArrayList<String>> temp0 = new ArrayList<>();
                        temp0.add(chGrammar);
                        chomskyRules.put(keys.get(k),temp0);
                    }
                    else
                        chomskyRules.get(keys.get(k)).add(chGrammar);
                }
            }
        }
        Grammar chGrammar =new Grammar(chomskyRules,chomskyVariables);
        return chGrammar;
    }
    Grammar changeToGreibachForm()
    {
//        if(!isGreibachNormalForm)
//            return changeToChomskyForm().changeToGreibachForm();
        System.out.println("____________________________________________________________________________");
        ArrayList<String> keys = setToArray(rules.keySet());
        for(int k=0;k<keys.size();k++)
        {
            ArrayList<ArrayList<String>> grammars = rules.get(keys.get(k));
            //remove leftRec for grammars;
            for (int c=0;c<grammars.size();c++)
            {
                if (grammars.get(c).get(0).equals(keys.get(k)))
                {
                    leftRec(keys.get(k));
                    keys.add("<"+keys.get(k).replace("<","").replace(">","")+"'>");
                    break;
                }
            }
        }
        for(int k=0;k<keys.size();k++)
        {
            boolean reDoFor = false;
            ArrayList<ArrayList<String>> grammars = rules.get(keys.get(k));
            //remove leftRec for grammars;
            for (int h = 0;h<grammars.size();h++)
            {
                ArrayList<String> grammar = grammars.get(h);
                if (grammar.get(0).contains("<"))
                {
                    ArrayList<ArrayList<String>> gr = rules.get(grammar.get(0));
                    for (int c=0; c<gr.size();c++)
                    {
                        reDoFor = true;
                        ArrayList<String> temp = cpArray(grammar);
                        temp.remove(0);
                        temp.addAll(0,gr.get(c));
                        if (!grammars.contains(temp))
                            grammars.add(temp);
                        if (gr.get(c).get(0).equals(keys.get(k)))
                        {
                            leftRec(keys.get(k));
                            keys.add("<"+keys.get(k).replace("<","").replace(">","")+"'>");
                        }
                    }
                    grammars.remove(grammar);
                }
            }
            if (reDoFor)
                k--;
        }
        return new Grammar(rules,keys);
    }
    void leftRec(String key)
    {
        ArrayList<ArrayList<String>> grammars = rules.get(key);
        ArrayList<ArrayList<String>> newGrammars = new ArrayList<>();
        ArrayList<ArrayList<String>> newGrammarsPrim = new ArrayList<>();
        ArrayList<String> newGrammar ;
        for (ArrayList<String> grammar : grammars)
        {
            newGrammar = new ArrayList<>();
            if (!grammar.get(0).equals(key))
            {
                newGrammar.addAll(grammar);
                newGrammar.add("<"+key.replace("<","").replace(">","")+"'>");
                newGrammars.add(newGrammar);
            }
            else
            {
                newGrammar.addAll(grammar.subList(1,grammar.size()));
                newGrammar.add("<"+key.replace("<","").replace(">","")+"'>");
                newGrammarsPrim.add(newGrammar);
            }
        }
        newGrammar = new ArrayList<>();
        newGrammar.add("#");
        newGrammarsPrim.add(newGrammar);
        rules.remove(key);
        rules.put(key,newGrammars);
        rules.put("<"+key.replace("<","").replace(">","")+"'>",newGrammarsPrim);
    }
//    boolean isGenerateByGrammar(String input)
//    {
//        if (!isDeleteTrash)
//            return deleteTrash().isGenerateByGrammar(input);
//        if (isChomskyForm)
//        {
//
//        }
//        else
//        {
//
//        }
//
//    }
    private ArrayList<String> setToArray(Set<String> set)
    {
        ArrayList<String> temp = new ArrayList<>();
        for (String string:set)
        {
            temp.add(string);
        }
        return temp;
    }
    private ArrayList<String> cpArray(ArrayList<String> arrayList)
    {
        ArrayList<String> temp = new ArrayList<>();
        for (String string:arrayList)
            temp.add(string);
        return temp;
    }
    ArrayList<String[]> findUnitProduction()
    {
        ArrayList<String[]> unitVariables = new ArrayList<>();
        for (String key:setToArray(rules.keySet()))
        {
            ArrayList<ArrayList<String>> grammars = rules.get(key);
            for (int c= 0;c<grammars.size();c++)
            {
                ArrayList<String> grammar = grammars.get(c);
                if (grammar.size() == 1) {
                    if (grammar.get(0).contains("<"))
                    {
                        if (!grammar.get(0).equals(key))
                        {
                            String[] temp = {key,grammar.get(0)};
                            unitVariables.add(temp);
                        }
                        grammars.remove(grammar);
                        c--;
                    }
                }
                if (grammars.size() == 0)
                    rules.remove(key);
            }
        }
        return unitVariables;
    }
    void printGrammar()
    {
        System.out.println(rules);
    }
}