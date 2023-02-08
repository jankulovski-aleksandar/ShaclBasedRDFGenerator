package com.company;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;


import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {

    static final String testPath="D:\\faks\\7mi semestar\\timski\\shacl_files\\shaclStart.ttl";

    public static void main(String[] args) throws FileNotFoundException {

        List<Triple> triplesList=convertShaclFileToTriples(testPath);

        //Printing triples
        //triplesList.forEach(triple -> {
        //    System.out.print(triple.getSubject()+"--->");
        //    System.out.print(triple.getPredicate()+"--->");
        //    System.out.println(triple.getObject()+"--->");});

        List<Object> properties=getShPropertiesFromTriples(triplesList);

        Map<Object,Map<Node,List<Node>>> map=new HashMap<>();

        properties.forEach(property->{
            List<Triple> shPropertyTriples=listPropertiesAsSubjects(property,triplesList);

           Map<Object, Map<Node, List<Node>>> tmp=createRestrictionMap(shPropertyTriples);

           Object key=new ArrayList<Object>(tmp.keySet()).get(0);
           map.put(key,tmp.get(key));
        });


        System.out.println("priting map\n");
        map.forEach((k,v)->{
            System.out.print("key " + k + "--->");
            v.forEach((k1,v1)->{
                System.out.print("key1 " + k1 + "--->");
                System.out.print("value1 " + v1 + "    ");
            });
            System.out.println();
        });

    }

    public static List<Triple> convertShaclFileToTriples(String filePath){
        Model model = RDFDataMgr.loadModel(filePath);
        model.write(System.out, "TTL");

        Graph shapesGraph = RDFDataMgr.loadGraph(filePath);
        Shapes shapes = Shapes.parse(shapesGraph);
        return shapes.getGraph().find().toList();
    }

    public static List<Object> getShPropertiesFromTriples(List<Triple> triples){
        return triples.stream()
                .filter(t->t.getPredicate().toString().contains("property"))
                .map((Function<Triple, Object>) Triple::getObject)
                .collect(Collectors.toList());
    }

    public static List<Triple> listPropertiesAsSubjects(Object property,List<Triple> triplesList){
        return triplesList.stream()
                .filter(triple -> triple.getSubject().equals(property))
                .collect(Collectors.toList());
    }

    public static Map<Object,Map<Node,List<Node>>> createRestrictionMap(List<Triple> shProperties){
        Map<Node,List<Node>> restrictions=new HashMap<>();
        AtomicReference<Node> path=new AtomicReference<>();

        shProperties.forEach(triple -> {
            if(!triple.getPredicate().toString().contains("path")){
                if(restrictions.containsKey(triple.getPredicate()))
                    restrictions.get(triple.getPredicate()).add(triple.getObject());
                else {
                    List<Node> list=new ArrayList<>();
                    list.add(triple.getObject());
                    restrictions.put(triple.getPredicate(), list);
                }
            }
            else
                path.set(triple.getObject());
        });
        Map<Object, Map<Node, List<Node>>> map=new HashMap<>();
        map.put(path,restrictions);
        return map;
    }
}
