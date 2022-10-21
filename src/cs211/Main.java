package cs211;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.awt.geom.Line2D;
import javax.imageio.ImageIO;
import javax.swing.*;

class Main{

    private static Dictionary dict = new Dictionary();
    private static String[] array = new String[dict.getSize()];
    private static ArrayList<String> sortedCoordinates = new ArrayList<>();
    private static Set<String> used = new HashSet<>();

    /*
    //Coordinate limits (Ireland's northern-most etc. points.)

    Northern most 55.384127822034834
    Southern most 51.447801314194336
    Western most -10.47653350127613
    Eastern most -5.433756678278688

     */

    public static void main(String[] args) throws IOException {

        //fill Array
        for(int i=0; i<array.length; i++){
            array[i] = dict.getWord(i);
        }

        shortestPath(array[120]);
        visualise();

    }

    private static JFrame frame;
    private static JLabel label;

    public static void display(BufferedImage image){
        if(frame==null){
            frame=new JFrame();
            frame.setTitle("stained_image");
            frame.setSize(image.getWidth(), image.getHeight());
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            label=new JLabel();
            label.setIcon(new ImageIcon(image));
            frame.getContentPane().add(label,BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);
        }else label.setIcon(new ImageIcon(image));
    }

    //This method gets the buffered image with all the data and displays it
    public static void visualise(){
        try {
            int imagePixelWidth = 850;
            int bubble_size = 5;
            BufferedImage bufferedImage = new BufferedImage(imagePixelWidth,
                    imagePixelWidth, BufferedImage.TYPE_INT_ARGB);


            for (int i=0; i<sortedCoordinates.size(); i++){

                String[] coordinate = sortedCoordinates.get(i).split(",",3);

                double lat = Double.parseDouble(coordinate[1]);
                double lon = Double.parseDouble(coordinate[2]);


                //double lat = 11.995724479140792;
                //double lon = 121.91359648077058;

                // min-max plotting lat/lon of your image
                double min_lat = 51.447801314194336;
                double min_lon = -10.47653350127613;
                double max_lat = 55.384127822034834;
                double max_lon = -5.433756678278688;

                Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();

                double latExtent = max_lat - min_lat;
                double lonExtent = max_lon - min_lon;

                double ly1 = (imagePixelWidth * (lat - min_lat)) / latExtent;
                double lx1 = (imagePixelWidth * (lon - min_lon)) / lonExtent;

                int ly = (int) (imagePixelWidth - ly1);// pixel increases downwards. Latitude increases upwards (north direction). So you need to inverse your mapping.
                int lx = (int) lx1;

                graphics.setColor(new Color(255, 0, 0));
                if(i==120){
                    graphics.setColor(new Color(0,0,255));
                }

                graphics.fillOval(lx - bubble_size / 2, ly - bubble_size / 2, bubble_size, bubble_size);
                if (i > 0){
                    String[] previousCoordinate = sortedCoordinates.get(i-1).split(",",3);

                    double lat2 = Double.parseDouble(previousCoordinate[1]);
                    double lon2 = Double.parseDouble(previousCoordinate[2]);

                    double ly2 = (imagePixelWidth * (lat2 - min_lat)) / latExtent;
                    double lx2 = (imagePixelWidth * (lon2 - min_lon)) / lonExtent;

                    int ly3 = (int) (imagePixelWidth - ly2);// pixel increases downwards. Latitude increases upwards (north direction). So you need to inverse your mapping.
                    int lx3 = (int) lx2;

                    graphics.draw(new Line2D.Double(lx - bubble_size / 2, ly - bubble_size / 2, lx3 - bubble_size / 2, ly3 - bubble_size / 2));
                }

                ImageIO.write(bufferedImage, "jpg", new File("src//map.jpg"));

                display(bufferedImage);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //This method calculates the shortest path using a greedy algorithm
    public static double shortestPath(String gps){

        System.out.println("(Maynooth) " + gps);

        String previous = gps;
        String current = gps;
        used.add(current);
        int distance = Integer.MAX_VALUE;

        for (int i=0; i<121; i++){
            previous = current;
            current = nearestPoint(current,used);
            used.add(current);
            if(i == 120) {System.out.print("(Maynooth) ");}


            //System.out.println(current + " Distance: " + getDistance(previous,current));
            distance += getDistance(previous,current);
            sortedCoordinates.add(current);
        }
        return distance;
    }

    //This methods gets the nearest point from the CURRENT point
    public static String nearestPoint(String gps, Set<String> used) {

        double shortestDistance = Integer.MAX_VALUE;
        int coordinate = 0;


        for (int i=0; i<121; i++) {
            double distance = getDistance(gps,array[i]);

            if(distance != 0.0 && distance < shortestDistance && !(used.contains(array[i]))) {
                shortestDistance = distance;
                coordinate = i;
            }
        }
        System.out.print(coordinate + ",");
        return array[coordinate];
    }

    //This methods simply calculates the distance between two points
    public static double getDistance(String gps1, String gps2){

        double earthRadius = 6371;

        String[] coordinate1 = gps1.split(",",3);
        String[] coordinate2 = gps2.split(",",3);


        double lat1 = Double.parseDouble(coordinate1[1]);
        double lon1 = Double.parseDouble(coordinate1[2]);

        double lat2 = Double.parseDouble(coordinate2[1]);
        double lon2 = Double.parseDouble(coordinate2[2]);

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lon2-lon1);

        double sindLat = Math.sin(dLat / 2);
        double sindLon = Math.sin(dLng / 2);

        //This equation allows us to account for earth's curve
        double a = Math.pow(sindLat, 2) + Math.pow(sindLon, 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist; //output distance in km
    }
}

//Dictionary Class used to read in a list of words
class Dictionary{

    private String input[];

    public Dictionary(){
        input = load("src\\120schools.txt");
    }

    public int getSize(){
        return input.length;
    }

    public String getWord(int n){
        return input[n].trim();
    }

    private String[] load(String file) {
        File aFile = new File(file);
        StringBuffer contents = new StringBuffer();
        BufferedReader input = null;
        try {
            input = new BufferedReader( new FileReader(aFile) );
            String line = null;
            int i = 0;
            while (( line = input.readLine()) != null){
                contents.append(line);
                i++;
                contents.append(System.getProperty("line.separator"));
            }
        }catch (FileNotFoundException ex){
            System.out.println("Can't find the file - are you sure the file is in this location: "+file);
            ex.printStackTrace();
        }catch (IOException ex){
            System.out.println("Input output exception while processing file");
            ex.printStackTrace();
        }finally{
            try {
                if (input!= null) {
                    input.close();
                }
            }catch (IOException ex){
                System.out.println("Input output exception while processing file");
                ex.printStackTrace();
            }
        }
        String[] array = contents.toString().split("\n");
        for(String s: array){
            s.trim();
        }
        return array;
    }
}