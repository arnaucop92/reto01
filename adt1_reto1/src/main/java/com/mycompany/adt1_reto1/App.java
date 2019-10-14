/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.adt1_reto1;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 *
 * @author arnau
 */
public class App {

    public static Double[] toDecimal(String latitude, String longitude) {

        String[] lat = latitude.replaceAll("[^0-9.\\s-]", "").split(" ");
        String[] lng = longitude.replaceAll("[^0-9.\\s-]", "").split(" ");
        Double dlat = toDecimal(lat);
        Double dlng = toDecimal(lng);
        return new Double[]{dlat, dlng};
    }

    public static Double toDecimal(String latOrLng) {

        String[] latlng = latOrLng.replaceAll("[^0-9.\\s-]", "").split(" ");
        Double dlatlng = toDecimal(latlng);
        return dlatlng;
    }

    public static Double toDecimal(String[] coord) {
        double d = Double.parseDouble(coord[0]);
        double m = Double.parseDouble(coord[1]);
        double s = Double.parseDouble(coord[2]);
        double signo = 1;
        if (coord[0].startsWith("-")) {
            signo = -1;
        }
        return signo * (Math.abs(d) + (m / 60.0) + (s / 3600.0));
    }

    public static void main(String[] args) throws ImageProcessingException, IOException {

        Scanner entrada = new Scanner(System.in);

        String latitud = null;
        String longitud = null;
        String latitudDec = "";
        String longitudDec = "";
        String origen;
        String destino;
        String mes = "";
        String anyo = "";
        String ciudad = "";
        int num;

        System.out.println("Introduce la ruta de origen:");
        origen = entrada.nextLine();
        File DirOrigen = new File(origen);
        File[] ficheros = DirOrigen.listFiles();
        System.out.println("Introduce la ruta de destino:");
        destino = entrada.nextLine();
        File DirDestino = new File(destino);
        do {
            System.out.println("1.-Organizar imágenes por fecha.");
            System.out.println("2.-Mostrar informacion de las coordenadas de las imágenes.");
            System.out.println("3.-Organizar imágenes por ciudades.");
            System.out.println("4.-Copias.");
            System.out.println("0.-Salir.");
            num = entrada.nextInt();
            entrada.nextLine();
            switch (num) {

                case 1:

                    DirDestino.mkdir();
                    for (int contFiles = 0; contFiles < ficheros.length; contFiles++) {

                        Metadata metadata = ImageMetadataReader.readMetadata(ficheros[contFiles]);
                        ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

                        for (Directory directory2 : metadata.getDirectories()) {

                            for (Tag tag : directory2.getTags()) {

                                if (tag.getTagName().contains("Date/Time Digitized")) {
                                    anyo = tag.getDescription().substring(0, 4);
                                    mes = tag.getDescription().substring(5, 7);
                                }
                            }
                        }

                        try {
                            File DirNueva = new File(destino + "\\" + anyo + mes);
                            DirNueva.mkdir();
                            InputStream in = new FileInputStream(origen + "\\" + ficheros[contFiles].getName());
                            OutputStream out = new FileOutputStream(DirNueva + "\\" + ficheros[contFiles].getName());

                            byte[] buf = new byte[1024];
                            int len;

                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }

                            in.close();
                            out.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }

                    }
                    System.out.println("Archivos copiados. Pulse intro para seguir:");
                    String intro = entrada.nextLine();
                    break;

                case 2:
                    File[] ficheros2 = DirOrigen.listFiles();
                    for (int contFiles = 0; contFiles < ficheros2.length; contFiles++) {

                        String fileSel = ficheros2[contFiles].getName();
                        Metadata metadata = ImageMetadataReader.readMetadata(ficheros2[contFiles]);
                        ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

                        for (Directory directory2 : metadata.getDirectories()) {

                            for (Tag tag : directory2.getTags()) {
                                if (tag.toString().contains("GPS")) {
                                    if (tag.getTagName().contains("GPS Latitude")) {
                                        latitud = tag.getDescription();
                                    } else if (tag.getTagName().contains("GPS Longitude")) {
                                        longitud = tag.getDescription();
                                    }
                                }
                            }
                        }
                        System.out.println("NOMBRE: " + fileSel + "   COORDENADAS: -" + "Lat: " + latitud + " -Lon: " + longitud);
                    }
                    break;

                case 3:
                    DirDestino.mkdir();
                    for (int contFiles = 0; contFiles < ficheros.length; contFiles++) {

                        Metadata metadata = ImageMetadataReader.readMetadata(ficheros[contFiles]);
                        ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

                        for (Directory directory2 : metadata.getDirectories()) {

                            for (Tag tag : directory2.getTags()) {
                                if (tag.toString().contains("GPS")) {
                                    if (tag.getTagName().contains("GPS Latitude")) {
                                        latitud = tag.getDescription();
                                    } else if (tag.getTagName().contains("GPS Longitude")) {
                                        longitud = tag.getDescription();
                                    }
                                } else if (tag.getTagName().contains("Date/Time Digitized")) {
                                    anyo = tag.getDescription().substring(0, 4);
                                    mes = tag.getDescription().substring(5, 7);
                                }
                            }
                        }
                        Double[] coord = toDecimal(latitud, longitud);
                        latitudDec = coord[0].toString().substring(0, 5);
                        longitudDec = coord[1].toString().substring(0, 5);

                        Scanner scn = null;

                        scn = new Scanner(new FileReader(new File("ES.txt")));
                        while (scn.hasNext()) {
                            String linea = scn.nextLine();

                            if (linea.contains(latitudDec) && linea.contains(longitudDec)) {
                                String[] parts = linea.split(";");
                                ciudad = parts[0];
                            }
                        }
                        try {
                            if (ciudad.equals("")) {
                                ciudad = "UbDesconocida";
                            }
                            File DirNueva = new File(destino + "\\" + ciudad);
                            DirNueva.mkdir();
                            InputStream in = new FileInputStream(origen + "\\" + ficheros[contFiles].getName());
                            OutputStream out = new FileOutputStream(DirNueva + "\\" + ficheros[contFiles].getName());

                            byte[] buf = new byte[1024];
                            int len;

                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }

                            in.close();
                            out.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                    System.out.println("Archivos copiados. Pulse intro para seguir:");
                    intro = entrada.nextLine();
                    break;
                    //
            }
        } while (num != 0);

    }

}
