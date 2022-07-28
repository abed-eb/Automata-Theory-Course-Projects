import java.io.File;
import java.io.IOException;
import java.util.*;
public class Main
{
    public static void main(String[] args)
    {
        try
        {
            Scanner sc = new Scanner(new File("src//data_gb.txt"));
            int n = Integer.parseInt(sc.nextLine());
            HashMap<String,ArrayList<ArrayList<String>>> rules = new HashMap<>();
            ArrayList<String> variables = new ArrayList<>();
            while (sc.hasNextLine())
            {
                String[] input = sc.nextLine().replaceAll("lambda","#").split("->");
                if (!variables.contains(input[0]))
                    variables.add(input[0].trim());
                //converting all lambdas to #
                String[] rightSide;
                if (input.length == 1)
                    rightSide = new String[]{"$"};
                //converting all spaces to $
                else
                    rightSide = input[1].split("\\|");
                ArrayList<ArrayList<String>> subRule = new ArrayList<>();
                for (String partition:rightSide)
                {
                    boolean isVariable = false;
                    ArrayList<String> arrayList = new ArrayList<>();
                    StringBuilder variable = new StringBuilder("<");
                    for (int k = 0 ; k<partition.length();k++)
                    {
                        if (partition.charAt(k) == ' ')
                            continue;
                        if (partition.charAt(k) == '<')
                        {
                            isVariable = true;
                            continue;
                        }
                        if (partition.charAt(k) == '>')
                        {
                            variable.append(">");
                            isVariable=false;
                            arrayList.add(variable.toString());
                            if (!variables.contains(variable.toString()))
                                variables.add(variable.toString().trim());
                            variable = new StringBuilder("<");
                            continue;
                        }
                        if (isVariable)
                            variable.append(partition.charAt(k));
                        else
                            arrayList.add(String.valueOf(partition.charAt(k)));
                    }
                    subRule.add(arrayList);
                }
                rules.put(input[0].trim(),subRule);
            }
            Grammar grammar = new Grammar(rules,variables);
            System.out.println("______________");
//            grammar.leftRec("<S>");
            grammar.changeToChomskyForm().changeToGreibachForm().printGrammar();
//            grammar.printGrammar();
//            System.out.println(grammar.isDeleteTrash);
//            grammar.printGrammar();
//            grammar.changeToChomskyForm().printGrammar();
            sc.close();
        }
        catch (IOException err)
        {
            System.err.println(":"+err);
        }
    }
}
