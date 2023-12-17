package Analizadores;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author Sebas
 */
public class Automata {
    public Nodo_binario arbol_de_expresion;
    private int num_nodo = 1;
    private ArrayList<Integer> estados_aceptacion = new ArrayList<>();
    private Set<String> estadosFinales;
    private Set<String> alfabeto;
    private final ArrayList<followposTabla> siguientes = new ArrayList<>();
    private final ArrayList<String> terminales = new ArrayList<>(); 
    private final ArrayList<ArrayList> transiciones = new ArrayList<>();
    private final ArrayList<ArrayList> tablaTransiciones = new ArrayList<>();
    private int nodos_afd = 1; 
    
    public Automata(Nodo_binario arbol_de_expresion) {
        this.arbol_de_expresion = arbol_de_expresion;
        
        Nodo_binario raiz = new Nodo_binario(".");
        Nodo_binario aceptacion = new Nodo_binario("#");
        aceptacion.setHoja(true);
        aceptacion.setAnulable(false);
        raiz.setHijo_derecho(aceptacion);
        raiz.setHijo_izquierdo(arbol_de_expresion);
        this.arbol_de_expresion=raiz;
        asignar_numeros(this.arbol_de_expresion);
        num_nodo = 0;
        metodo_arbol(this.arbol_de_expresion);
        String asd = r_crear_afnd(0,1,raiz.getHijo_izquierdo());
        //String asd = r_crear_afnd(0,1,this.arbol_de_expresion);
        System.out.println("digraph{\n" + graficar_arbol(this.arbol_de_expresion,num_nodo) + "\n}");
        System.out.println("digraph{\nrankdir=LR\nN1[shape=doublecircle]" + asd + "\n} ");
        generarDot();
        
        
        
    }
    
    public void generarDot() {
        //int contador = 0;
        String filenameArbol = "Arbol";
        //String filenameTablaSiguientes = "TablaSiguientes";
        String dotArbol = "digraph{\n" + graficar_arbol(this.arbol_de_expresion,num_nodo) + "\n}";
        //String dotSiguientes = "digraph{\n"+graficar_tabla_siguientes()+"\n}";
        try {
            DotToPngConverter.convertToPng("digraph{\n" + graficar_arbol(this.arbol_de_expresion,num_nodo) + "\n}", filenameArbol);
            //DotToPngConverter.convertToPng("digraph{\n"+graficar_tabla_siguientes()+"\n}", filenameTablaSiguientes);
        } catch (Exception e) {
            System.out.println("Ha ocurrido un error al generar el archivo de graphviz");
        }
        
    } 
    
    public String graficar_arbol(Nodo_binario nodo, int padre) {
        String s = "";
        num_nodo += 1;
        
        int actual = num_nodo;
        if (nodo ==  null) {
            num_nodo -=1;
            return s;
        }
        
        if (nodo.isHoja()) {
            //s+= "digraph{\n";
            s+= "n_" + num_nodo + "[label=<";
            s+= "<TABLE border=\"1\" cellspacing=\"2\" cellpading=\"9\" >\n"
                    +"<TR>\n";
                    if (nodo.isAnulable()) {
                        s+="<TD colspan=\"3\">"+"Anulable"+"</TD>\n";
                    }
                    else {
                        s+="<TD colspan=\"3\">"+"No es anulable"+"</TD>\n";
                    }
                    
                    s+="</TR>\n"
                    +"<TR>\n"
                    +"<TD>"+nodo.getPrimeros()+"    "+nodo.getDato()+"    " +nodo.getUltimos()+"</TD>\n"
                    +"</TR>\n"
                    +"<TR>\n"
                    +"<TD colspan=\"3\">"+nodo.getIdentificador()+"</TD>\n" 
                    +"</TR>\n"
                    +"</TABLE>>];\n";
            //System.out.println(s);
      
        } else {
            s+= "n_" + num_nodo + "[label=<";
            s+= "<TABLE border=\"1\" cellspacing=\"2\" cellpading=\"9\" >\n"
                    +"<TR>\n";
                    if (nodo.isAnulable()) {
                        s+="<TD colspan=\"3\">"+"Anulable"+"</TD>\n";
                    }
                    else {
                        s+="<TD colspan=\"3\">"+"No es anulable"+"</TD>\n";
                    }
                    //+"<TD colspan=\"3\">"+nodo.isAnulable()+"</TD>\n"
                    s+="</TR>\n"
                    +"<TR>\n"
                    +"<TD>"+nodo.getPrimeros()+"    "+nodo.getDato()+"    " +nodo.getUltimos()+"</TD>\n"
                    +"</TR>\n"
                    +"<TR>\n"
                    +"<TD colspan=\"3\">"+nodo.getIdentificador()+"</TD>\n" 
                    +"</TR>\n"
                    +"</TABLE>>];\n";
            //System.out.println(s);
            
        }
        if (padre != 0) {
            s+= "n_" + padre + "-> n_" + actual + ";\n";
        }
        s+=graficar_arbol(nodo.getHijo_izquierdo(),actual);
        s+=graficar_arbol(nodo.getHijo_derecho(),actual);
        //System.out.println(s);
        return s;
    }
    
    public void metodo_arbol(Nodo_binario actual) {
        if (actual == null) {
            return;
        }
        
        if (actual.isHoja()) {
            // Agregar ids propios
            actual.getPrimeros().add(actual.getIdentificador());
            // El ultimo es el num de hoja
            actual.getUltimos().add(actual.getIdentificador());
            return;
        }
        
        metodo_arbol(actual.getHijo_izquierdo());
        metodo_arbol(actual.getHijo_derecho());
        
        if(actual.getDato().equals("*")) {
            actual.setAnulable(true);
            actual.getPrimeros().addAll(actual.getHijo_derecho().getPrimeros());
            // Ultimos del primer hijo
            actual.getUltimos().addAll(actual.getHijo_derecho().getPrimeros());
            for (int i = 0; i < actual.getHijo_derecho().getPrimeros().size(); i++) {
                for (int j = 0; j < actual.getHijo_derecho().getUltimos().size(); j++) {
                    siguientes.get(actual.getHijo_derecho().getPrimeros().get(i)-1).getSiguientes().add(actual.getHijo_derecho().getUltimos().get(j));
                }
            }            
        }
        else if(actual.getDato().equals("+")) {
            actual.setAnulable(actual.getHijo_derecho().isAnulable());
            actual.getPrimeros().addAll(actual.getHijo_derecho().getPrimeros());
            for (int i = 0; i < actual.getHijo_derecho().getPrimeros().size(); i++) {
                for (int j = 0; j < actual.getHijo_derecho().getUltimos().size(); j++) {
                    siguientes.get(actual.getHijo_derecho().getPrimeros().get(i)-1).getSiguientes().add(actual.getHijo_derecho().getUltimos().get(j));
                }
            }
        }
        else if(actual.getDato().equals("?")) {
            // Si es anulable
            actual.setAnulable(true);
            actual.getPrimeros().addAll(actual.getHijo_derecho().getPrimeros());
            actual.getUltimos().addAll(actual.getHijo_derecho().getPrimeros());
        }
        else if(actual.getDato().equals("|")) {
           // Or
            actual.setAnulable(actual.getHijo_izquierdo().isAnulable() || actual.getHijo_derecho().isAnulable());
            actual.getPrimeros().addAll(actual.getHijo_izquierdo().getPrimeros());
            actual.getPrimeros().addAll(actual.getHijo_derecho().getPrimeros());
            actual.getUltimos().addAll(actual.getHijo_izquierdo().getUltimos());
            actual.getUltimos().addAll(actual.getHijo_derecho().getUltimos());
        }
        else if(actual.getDato().equals(".")) {
            // Concatenacion
            actual.setAnulable(actual.getHijo_izquierdo().isAnulable() && actual.getHijo_derecho().isAnulable());
            if(actual.getHijo_izquierdo().isAnulable()) {
                actual.getPrimeros().addAll(actual.getHijo_izquierdo().getPrimeros());
                actual.getPrimeros().addAll(actual.getHijo_derecho().getPrimeros());
            }
            else{
                actual.getPrimeros().addAll(actual.getHijo_izquierdo().getPrimeros());
            }
            if(actual.getHijo_derecho().isAnulable()) {
                actual.getUltimos().addAll(actual.getHijo_derecho().getUltimos());
                actual.getUltimos().addAll(actual.getHijo_izquierdo().getUltimos());
            }
            else {
                actual.getUltimos().addAll(actual.getHijo_derecho().getUltimos());
            }
           for (int i = 0; i < actual.getHijo_izquierdo().getUltimos().size(); i++) {
                for (int j = 0; j < actual.getHijo_derecho().getPrimeros().size(); j++) {
                    siguientes.get(actual.getHijo_izquierdo().getUltimos().get(i)-1).getSiguientes().add(actual.getHijo_derecho().getPrimeros().get(j));
                }
                
            }
        }
        
    }
    
    public void metodo_arbol2(Nodo_binario actual){
        if (actual == null ||actual.getHijo_izquierdo() ==null ) {
            return;
        }

        metodo_arbol2(actual.hijo_izq);
        metodo_arbol2(actual.hijo_der);

        if (actual.hoja) {
            actual.anulable = false;
            actual.primeros.add(actual.identificador);
            actual.ultimos.add(actual.identificador);
        } else {
            switch (actual.dato) {
                case "*" -> {
                    actual.anulable = true;
                    actual.primeros.addAll(actual.hijo_izq.primeros);
                    actual.ultimos.addAll(actual.hijo_izq.ultimos);
                    
                }
                case "?" -> {
                    actual.anulable = true;
                    actual.primeros.addAll(actual.hijo_izq.primeros);
                    actual.ultimos.addAll(actual.hijo_izq.ultimos);
                }
                case "+" -> {
                    actual.anulable = actual.hijo_izq.anulable;
                    actual.primeros.addAll(actual.hijo_izq.primeros);
                    actual.ultimos.addAll(actual.hijo_izq.ultimos);
                }
                case "|" -> {
                    actual.anulable = actual.hijo_izq.anulable || actual.hijo_der.anulable;
                    actual.primeros.addAll(actual.hijo_izq.primeros);
                    actual.primeros.addAll(actual.hijo_der.primeros);
                    actual.ultimos.addAll(actual.hijo_izq.ultimos);
                    actual.ultimos.addAll(actual.hijo_der.ultimos);
                }
                case "." -> {
                    actual.anulable = actual.hijo_izq.anulable && actual.hijo_der.anulable;
                    if (actual.hijo_izq.anulable) {
                        actual.primeros.addAll(actual.hijo_izq.primeros);
                        actual.primeros.addAll(actual.hijo_der.primeros);
                    } else {
                        actual.primeros.addAll(actual.hijo_izq.primeros);
                    }
                    if (actual.hijo_der.anulable) {
                        actual.ultimos.addAll(actual.hijo_izq.ultimos);
                        actual.ultimos.addAll(actual.hijo_der.ultimos);
                    } else {
                        actual.ultimos.addAll(actual.hijo_der.ultimos);
                    }
                 
                }
            }
        }
    }
    
    
    public String r_crear_afnd(int inicio,int fin,Nodo_binario actual) {
        String resultado = "";
        if (actual == null) {
            return resultado;
        }
        if(actual.hoja){
            System.out.println(actual.dato);
            resultado = "N"+inicio+"->N"+fin+"[label=\""+actual.dato.replaceAll("\"", "")+"\"];\n";
    }
        else{
            switch(actual.dato){
                case "." -> {
                    nodos_afd += 1;
                    int anterior = nodos_afd;
                    resultado += "N"+nodos_afd+"[label=\"\"];\n";
                    resultado += r_crear_afnd(inicio,anterior,actual.getHijo_izquierdo());
                    resultado += r_crear_afnd(anterior,fin,actual.getHijo_derecho());
                }
                case "|" ->{
                    nodos_afd += 1;
                    resultado += "N"+nodos_afd+"[label=\"\"];\n";
                    resultado += "N"+inicio+"->N"+nodos_afd+"[label=\"e\"];\n";
                    nodos_afd += 1;
                    resultado += "N"+nodos_afd+"[label=\"\"];\n";
                    resultado += "N"+nodos_afd+"->N"+fin+"[label=\"e\"];\n";
                    resultado += r_crear_afnd(nodos_afd-1,nodos_afd,actual.getHijo_izquierdo());
                    nodos_afd += 1;
                    resultado += "N"+nodos_afd+"[label=\"\"];\n";
                    resultado += "N"+inicio+"->N"+nodos_afd+"[label=\"e\"];\n";
                    nodos_afd += 1;
                    resultado += "N"+nodos_afd+"[label=\"\"];\n";
                    resultado += "N"+nodos_afd+"->N"+fin+"[label=\"e\"];\n";
                    resultado += r_crear_afnd(nodos_afd-1,nodos_afd,actual.getHijo_derecho());
                }
                case "+" ->{
                    nodos_afd += 1;
                    resultado += "N"+nodos_afd+"[label=\"\"];\n";
                    resultado += "N"+inicio+"->N"+nodos_afd+"[label=\"e\"];\n";
                    nodos_afd += 1;
                    resultado += "N"+nodos_afd+"[label=\"\"];\n";
                    resultado += "N"+nodos_afd+"->N"+fin+"[label=\"e\"];\n";
                    resultado += "N"+nodos_afd+"->N"+(nodos_afd-1)+"[label=\"e\"];\n";
                    resultado += r_crear_afnd(nodos_afd-1,nodos_afd,actual.getHijo_derecho());
                }
                case "*" ->{
                    resultado += "N"+inicio+"->N"+fin+"[label=\"e\"];\n";
                    nodos_afd += 1;
                    resultado += "N"+nodos_afd+"[label=\"\"];\n";
                    resultado += "N"+inicio+"->N"+nodos_afd+"[label=\"e\"];\n";
                    nodos_afd += 1;
                    resultado += "N"+nodos_afd+"[label=\"\"];\n";
                    resultado += "N"+nodos_afd+"->N"+fin+"[label=\"e\"];\n";
                    resultado += "N"+nodos_afd+"->N"+(nodos_afd-1)+"[label=\"e\"];\n";
                    resultado += r_crear_afnd(nodos_afd-1,nodos_afd,actual.getHijo_derecho());
                }
                case "?" ->{
                    resultado += "N"+inicio+"->N"+fin+"[label=\"e\"];\n";
                    nodos_afd += 1;
                    resultado += "N"+nodos_afd+"[label=\"\"];\n";
                    resultado += "N"+inicio+"->N"+nodos_afd+"[label=\"e\"];\n";
                    nodos_afd += 1;
                    resultado += "N"+nodos_afd+"[label=\"\"];\n";
                    resultado += "N"+nodos_afd+"->N"+fin+"[label=\"e\"];\n";
                    resultado += r_crear_afnd(nodos_afd-1,nodos_afd,actual.getHijo_derecho());
            }
        }
    }
        return resultado;
    }
    
    
    public void asignar_numeros(Nodo_binario actual) {
        if (actual == null) {
            return;
        }
        if(actual.isHoja()) {
            //System.out.println("Mensaje de prueba!!!!!!");
            actual.setIdentificador(num_nodo);
            num_nodo++;
            siguientes.add(new followposTabla(actual.getDato(), actual.getIdentificador()));
            if (!terminales.contains(actual.getDato()) && !actual.getDato().equals("#")) {
                terminales.add(actual.getDato());
            return;
        }
        }
        asignar_numeros(actual.getHijo_izquierdo());
        asignar_numeros(actual.getHijo_derecho());
    
    }

    public Nodo_binario getArbol_de_expresion() {
        return arbol_de_expresion;
    }

    public void setArbol_de_expresion(Nodo_binario arbol_de_expresion) {
        this.arbol_de_expresion = arbol_de_expresion;
    }
    
    public int obtener_siguiente(int estado, String entrada) {
        ArrayList<Integer> trans = this.transiciones.get(estado);
        int idx = this.terminales.indexOf(entrada);
        return trans.get(idx);
    }
}
    
   


