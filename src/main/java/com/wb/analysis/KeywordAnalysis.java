package com.wb.analysis;

import com.wb.analysis.bean.AnalysisTree;
import com.wb.es.bean.BaseFilter;
import com.wb.es.bean.BoolFilter;
import com.wb.es.bean.MatchPhraseFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by wb on 2018/11/29
 */
public class KeywordAnalysis {
    private String expression;

    public KeywordAnalysis(String expression) {
        this.expression = expression.replaceAll("（", "(")
            .replaceAll("－", "-")
            .replaceAll("＋", "+")
            .replaceAll("）", ")")
            .replaceAll("｜", "|")
            .replaceAll(" ", "")
            .replaceAll("　", "");
    }

    //将后缀表达式转化成树
    public AnalysisTree convertOriginalTree() {
        List<String> reversePolish = expression2Reverse();

        Stack<AnalysisTree> analysisTrees = new Stack<>();

        AnalysisTree top;
        AnalysisTree second;
        for (int i = 0; i < reversePolish.size(); i++) {

            switch (reversePolish.get(i)) {
                case ("-"):
                    top = analysisTrees.pop();
                    if (top.getMust_not() != null && top.getMust_not().size() != 0) {
                        second = analysisTrees.pop();
                        top.getMust_not().add(second);
                        analysisTrees.push(top);
                    } else {
                        AnalysisTree analysisTree = new AnalysisTree();

                        List<AnalysisTree> mustNot = new ArrayList<>();
                        mustNot.add(top);
                        analysisTree.setMust_not(mustNot);

                        analysisTrees.push(analysisTree);
                    }
                    break;
                case ("+"):
                    top = analysisTrees.pop();
                    second = analysisTrees.pop();
                    if (top.getMust() != null && top.getMust().size() != 0) {

                        top.getMust().add(second);
                        analysisTrees.push(top);
                    } else {
                        AnalysisTree analysisTree = new AnalysisTree();
                        List<AnalysisTree> must = new ArrayList<>();
                        must.add(top);
                        must.add(second);
                        analysisTree.setMust(must);

                        analysisTrees.push(analysisTree);
                    }
                    break;
                case ("|"):
                    top = analysisTrees.pop();
                    second = analysisTrees.pop();
                    if (top.getShould() != null && top.getShould().size() != 0) {
                        //second = analysisTrees.pop();
                        top.getShould().add(second);
                        analysisTrees.push(top);
                    } else {
                        AnalysisTree analysisTree = new AnalysisTree();

                        List<AnalysisTree> should = new ArrayList<>();
                        should.add(top);
                        should.add(second);
                        analysisTree.setShould(should);

                        analysisTrees.push(analysisTree);
                    }
                    break;
                default:
                    analysisTrees.push(getAnalysisTree(reversePolish.get(i)));
                    break;
            }
        }
        top = analysisTrees.pop();

        /*while (!analysisTrees.empty()) {
            if (top.getMust() != null && top.getMust().size() != 0) {
                top.getMust().add(analysisTrees.pop());
            } else {
                List<AnalysisTree> must = new ArrayList<>();
                must.add(top);
                while (!analysisTrees.empty()) {
                    must.add(analysisTrees.pop());
                }
                AnalysisTree result = new AnalysisTree();

                result.setMust(must);
                return result;
            }
        }*/
        return top;
    }

    //获取优化结构后的树
    public AnalysisTree convertOptimizationTree() {
        AnalysisTree as = convertOriginalTree();
        if (as.isLeaf()) {
            AnalysisTree newAs = new AnalysisTree();
            List<AnalysisTree> must = new ArrayList<>();
            must.add(as);
            newAs.setMust(must);

            return  newAs;
        } else {
            optimization(as);
        }
        return as;
    }

    //中缀表达式转后缀
    private List<String> expression2Reverse() {
        char[] expressionChars = expression.toCharArray();
        Stack<Character> opStack = new Stack<>();
        List<String> reversePolish = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder("");

        for (int i = 0; i < expressionChars.length; i++) {
            switch (expressionChars[i]) {
                case ('-'):
                case ('+'):
                case ('|'):
                    addWord(reversePolish, stringBuilder.toString());

                    int currentLevel = getOpLevel(expressionChars[i]);

                    if (!opStack.empty() && (currentLevel < getOpLevel(opStack.peek()))) {

                        while (!opStack.empty() && !(currentLevel > getOpLevel(opStack.peek()) && !opStack.peek().equals('('))) {
                            reversePolish.add(opStack.pop() + "");
                        }
                    }
                    opStack.push(expressionChars[i]);
                    stringBuilder.setLength(0);
                    break;
                case ('('):
                    opStack.push(expressionChars[i]);
                    break;
                case (')'):
                    addWord(reversePolish, stringBuilder.toString());
                    stringBuilder.setLength(0);

                    while (!opStack.empty() && !opStack.peek().equals('(')) {
                        reversePolish.add(opStack.pop() + "");
                    }
                    opStack.pop();
                    break;
                default:
                    stringBuilder.append(expressionChars[i]);
                    break;
            }
            if (i == expressionChars.length - 1) {
                addWord(reversePolish, stringBuilder.toString());
            }
        }

        while (!opStack.empty()) {
            reversePolish.add("" + opStack.pop());
        }

        //补足a-b时间上是a且非b的表达符号缺失
        for (int i = 0; i < reversePolish.size(); i++) {
            if (reversePolish.get(i).equals("-")){
                if(reversePolish.size() - 1 > i && !"-".equals(reversePolish.get(i + 1))) {
                    reversePolish.add(i + 1, "+");
                }
            }
        }

        //同上，如果最后一个符号是-，加上缺失的+号
        if ("-".equals(reversePolish.get(reversePolish.size() - 1)) && reversePolish.size() > 2) {
            reversePolish.add("+");
        }

        return reversePolish;
    }

    //优化树结构
    private void optimization(AnalysisTree analysisTree) {

        List<AnalysisTree> must = analysisTree.getMust();
        if (must == null) {
            must = new ArrayList<>();
            analysisTree.setMust(must);
        }
        List<AnalysisTree> mustNot = analysisTree.getMust_not();
        if (mustNot == null) {
            mustNot = new ArrayList<>();
            analysisTree.setMust_not(mustNot);
        }

        for (int i = 0; i < must.size(); i++) {
            //将子节点must中的must_not合并到父节点的must_not(即must的兄弟节点)
            AnalysisTree child = must.get(i);
            //

            if (child.getMust_not() != null) {
                mustNot.addAll(child.getMust_not());
                child.setMust_not(null);
                must.remove(child);
                i--;
            }

            if (child.getMust() != null) {
                must.addAll(child.getMust());
                child.setMust(null);
                must.remove(child);
                i--;
            }
            optimization(child);
        }
        //return analysisTree;
    }

    //将后缀表达式转化成es查询语句
    public BoolFilter convertEsStatement() {
        BoolFilter boolFilter = convertBoolFilter(convertOptimizationTree(), new BoolFilter());
        return boolFilter;
    }

    private BoolFilter convertBoolFilter(AnalysisTree analysisTree, BoolFilter boolFilter) {

        if (analysisTree.getShould() != null && !analysisTree.isLeaf()) {
            for (int i = 0; i < analysisTree.getShould().size(); i++) {
                AnalysisTree childTree = analysisTree.getShould().get(i);
                if (analysisTree.getShould().get(i).isLeaf()) {
                    BoolFilter child = getBoolFilter(childTree.getWord());
                    boolFilter.addShould(child);
                } else {
                    BoolFilter childNode = new BoolFilter();
                    boolFilter.addShould(childNode);
                    convertBoolFilter(analysisTree.getShould().get(i), childNode);
                }
            }
        }

        if (analysisTree.getMust() != null && !analysisTree.isLeaf()) {
            for (int i = 0; i < analysisTree.getMust().size(); i++) {
                if (analysisTree.getMust().get(i).isLeaf()) {
                    BoolFilter child = getBoolFilter(analysisTree.getMust().get(i).getWord());
                    boolFilter.addMust(child);
                } else {
                    BoolFilter childNode = new BoolFilter();
                    boolFilter.addMust(childNode);
                    convertBoolFilter(analysisTree.getMust().get(i),childNode);
                }
            }
        }

        if (analysisTree.getMust_not() != null && !analysisTree.isLeaf()) {
            for (int i = 0; i < analysisTree.getMust_not().size(); i++) {
                if (analysisTree.getMust_not().get(i).isLeaf()) {
                    BoolFilter child = getBoolFilter(analysisTree.getMust_not().get(i).getWord());
                    boolFilter.addMustNot(child);
                } else {
                    BoolFilter childNode = new BoolFilter();
                    boolFilter.addMustNot(childNode);
                    convertBoolFilter(analysisTree.getMust_not().get(i),childNode);
                }
            }
        }

        return boolFilter;
    }

    private AnalysisTree getAnalysisTree(String value) {
        AnalysisTree analysisTree = new AnalysisTree();
        analysisTree.setWord(value);
        analysisTree.setLeaf(true);
        return analysisTree;
    }

    private BoolFilter getBoolFilter(String value) {
        BoolFilter innerFilter = new BoolFilter();
        List<BaseFilter> innerShouldList = new ArrayList<>();
        innerFilter.setShould(innerShouldList);
        innerShouldList.add(new MatchPhraseFilter("title", value));
        innerShouldList.add(new MatchPhraseFilter("content", value));
        return innerFilter;
    }

    private void addWord(List<String> list, String word) {
        if (word != null && !"".equals(word)) {
            list.add(word);
        }
    }

    //表达式关系符的优先级
    private static int getOpLevel(char c) {
        switch (c) {
            case '|':
                return 3;
            case '+':
                return 1;
            case '-':
                return 2;
            default:
                return 0;
        }
    }

    public static void main(String[] args) {
        //通过
        //String a = "((a+b+c)|(d+e))";

        //通过
        //String a = "(a+b+c+d+e)|(h+f)";

        //通过
        String a = "h+e+t-m+n-(z+v)-a-b-c-d|y+nihao-hundan+haonimei";

        //通过

        //todo, -括号里面紧接着一个-的暂时不支持，可以对比原式解决，感觉没有应用场景
        //a= "t - (h-a-b)";


        //a = "((a+b+c)|(d+e))";

        //通过
        //String a = "(a|b|c)-(d|e|f)";

        //通过
        //String a = "a|b|(h+t) +c+d";

        //String a = "a+b+c+e|c|d";
        //通过
        //a = "a|b|c-d+e-f";

        //a = "a-(b+c)";

        //通过
        //a = "a|b|c - d|e";

        //通过
        //a = "a";

        //通过
        //a = "a";

        //通过
        //a = "d+(a|b|c)-z-v-f-t-m+u -q|w";

        //a = "a-b|c";

        //a = "a|b|c - d|e|f";

        //a = "h+e+t-m+n-(z+v)-a-b-c-d|y+nihao-hundan+haonimei";

        //通过
        //a = "(a+b+c) - (d + e)";

        //通过
        //a = "a|b|c - (d+e)";

        //a = "e|f|(h-a-b)";

        //a = "((a+b+c)|(d+e))";

        //a = "((a+b-c)|(a+d))";

        //a = "a-(c-b)";

        //a= "t - (h+a-b)";

        a = "(山亭|于明傲)+(中学|学校|31中|三十一中|孤儿)+(打死|死因|上吊|自杀)";

        //a = "(a+b+c+d+e)|(h+f)";
        KeywordAnalysis kp = new KeywordAnalysis(a);
        List<String> result = kp.expression2Reverse();
        for (String str : result) {
            System.out.print(str + " ");
        }
        AnalysisTree results = kp.convertOriginalTree();
        //System.out.println(JSON.toJSON(results));

        System.out.println("合并后");
        kp.optimization(results);
        //System.out.println(JSON.toJSON(results));

        AnalysisTree results2 = kp.convertOptimizationTree();
        //System.out.println(JSON.toJSON(results2));

        BoolFilter boolFilter = kp.convertEsStatement();
        //System.out.println(JSON.toJSON(boolFilter));
        /*
        System.out.println(JSON.toJSON(results));
        for (String str : result) {
            System.out.print(str + " ");
        }
        System.out.println("");*/
        //System.out.println(JSON.toJSON(op));
    }
}
