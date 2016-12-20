package com.theironyard.tesseract;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by kdrudy on 10/19/16.
 */
public class Main {

    public static void main(String args[]) {

        Spark.init();
        Spark.get("/", ((request, response) -> {
            HashMap m = new HashMap();

            Session session = request.session();

            Optional<String> text = Optional.ofNullable(session.attribute("text"));
            if (text.isPresent()) {
                m.put("text", text.get());
            }

            return new ModelAndView(m, "home.html");
        }), new MustacheTemplateEngine());

        Spark.post("/ocr", ((request, response) -> {

            URL imageLink = new URL(request.queryParams("imageLink"));
            BufferedImage img = ImageIO.read(imageLink);

            ITesseract instance = new Tesseract();
            instance.setDatapath("/usr/local/share/");
            try {
                String result = instance.doOCR(img);
                System.out.println(result);

                Session session = request.session();
                session.attribute("text", escape(result));
            } catch (Exception e) {
                e.printStackTrace();
            }



            response.redirect("/");
            return "";
        }));
    }

    public static String escape(String s) {
        StringBuilder builder = new StringBuilder();
        boolean previousWasASpace = false;
        for( char c : s.toCharArray() ) {
            if( c == ' ' ) {
                if( previousWasASpace ) {
                    builder.append("&nbsp;");
                    previousWasASpace = false;
                    continue;
                }
                previousWasASpace = true;
            } else {
                previousWasASpace = false;
            }
            switch(c) {
                case '<': builder.append("&lt;"); break;
                case '>': builder.append("&gt;"); break;
                case '&': builder.append("&amp;"); break;
                case '"': builder.append("&quot;"); break;
                case '\n': builder.append("<br>"); break;
                // We need Tab support here, because we print StackTraces as HTML
                case '\t': builder.append("&nbsp; &nbsp; &nbsp;"); break;
                default:
                    if( c < 128 ) {
                        builder.append(c);
                    } else {
                        builder.append("&#").append((int)c).append(";");
                    }
            }
        }
        return builder.toString();
    }
}