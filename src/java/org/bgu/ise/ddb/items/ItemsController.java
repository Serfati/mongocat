package org.bgu.ise.ddb.items;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bgu.ise.ddb.MediaItems;
import org.bgu.ise.ddb.ParentController;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;


@RestController
@RequestMapping(value = "/items")
public class ItemsController extends ParentController {

    private MongoClient mongoClient = null;

    public MongoDatabase connectToDB() {
        mongoClient = null;
        mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://serfata:1122@sandbox.6lpfa.mongodb.net/admin?w=majority"));
        return mongoClient.getDatabase("adt");
    }

    private Connection conn = null;
    private final String username = "galbo";
    private final String password = "abcd";
    private final String connectionUrl = "jdbc:oracle:thin:@ora1.ise.bgu.ac.il:1521/oracle";
    private final String driver = "oracle.jdbc.driver.OracleDriver";

    /**
     * The function makes the connection to the DB
     */
    private void connect() {
        try {
            Class.forName(this.driver); //registration of the driver
            this.conn = DriverManager.getConnection(this.connectionUrl, this.username, this.password);//connection
            conn.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The function closes the connection to the DB
     */
    private void disconnect() {
        try {
            this.conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * The function copy all the items(title and production year) from the Oracle table MediaItems to the System storage.
     * The Oracle table and data should be used from the previous assignment
     */
    @RequestMapping(value = "fill_media_items", method = {RequestMethod.GET})
    public void fillMediaItems(HttpServletResponse response) {
        System.out.println("was here");
        try {
            MongoDatabase db = connectToDB();
            MongoCollection<Document> dbCollection = db.getCollection("media_items");
            ArrayList<Document> mis = getMediaItems();
            dbCollection.insertMany(mis);
            HttpStatus status = HttpStatus.OK;
            response.setStatus(status.value());
            mongoClient.close();
        } catch (Exception e) {
            e.printStackTrace();
            HttpStatus status = HttpStatus.CONFLICT;
            response.setStatus(status.value());
            mongoClient.close();
        }
    }

    /**
     * The function print the MEDIAITEMS's values
     */
    private ArrayList<Document> getMediaItems() {
        ArrayList<Document> mis = new ArrayList<>();
        if (this.conn == null) connect();
        PreparedStatement ps = null;
        String query = "SELECT * FROM MEDIAITEMS"; //query
        try {
            ps = conn.prepareStatement(query); //compiling query in the DB
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Document mi = new Document();
                mi.put("title", rs.getString("TITLE"));
                mi.put("prod_year", "" + rs.getInt("PROD_YEAR"));
                mis.add(mi);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e3) {
                e3.printStackTrace();
            }
        }
        return mis;
    }


    /**
     * The function copy all the items from the remote file,
     * the remote file have the same structure as the films file from the previous assignment.
     * You can assume that the address protocol is http
     *
     * @throws IOException
     */
    @RequestMapping(value = "fill_media_items_from_url", method = {RequestMethod.GET})
    public void fillMediaItemsFromUrl(@RequestParam("url") String urladdress,
                                      HttpServletResponse response) throws IOException {
        System.out.println(urladdress);
        try {
            MongoDatabase db = connectToDB();
            MongoCollection<Document> dbCollection = db.getCollection("media_items");
            ArrayList<Document> mis = CSVReader(urladdress);
            dbCollection.insertMany(mis);
            HttpStatus status = HttpStatus.OK;
            response.setStatus(status.value());
            mongoClient.close();
        } catch (Exception e) {
            e.printStackTrace();
            HttpStatus status = HttpStatus.CONFLICT;
            response.setStatus(status.value());
            mongoClient.close();
        }
    }

    private ArrayList<Document> CSVReader(String path) throws IOException {
        URL csvFile = new URL(path);
        String line;
        String cvsSplitBy = ",";
        ArrayList<Document> mis = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.openStream()))) {
            while ((line = br.readLine()) != null) {
                String[] movie = line.split(cvsSplitBy);
                if (movie.length == 2) {
                    Document mi = new Document();
                    mi.put("title", movie[0]);
                    if (isInteger(movie[1])) {
                        mi.put("prod_year", movie[1]);
                        mis.add(mi);
                    }
                } else System.out.println("CSV doenst have any movies");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mis;

    }

    /**
     * The function retrieves from the system storage N items,
     * order is not important( any N items)
     *
     * @param topN - how many items to retrieve
     * @return
     */
    @RequestMapping(value = "get_topn_items", headers = "Accept=*/*", method = {RequestMethod.GET}, produces = "application/json")
    @ResponseBody
    @org.codehaus.jackson.map.annotate.JsonView(MediaItems.class)
    public MediaItems[] getTopNItems(@RequestParam("topn") int topN) {
        MongoDatabase db = connectToDB();
        MongoCollection<Document> dbCollection = db.getCollection("media_items");
        ArrayList<MediaItems> mis = new ArrayList<>();
        for (Document cur : dbCollection.find())
            if (topN > 0) {
                String title = cur.getString("title");
                String prodYear = cur.getString("prod_year");
                MediaItems mi = new MediaItems(title, Integer.parseInt(prodYear));
                mis.add(mi);
                topN--;
            } else break;
        MediaItems[] miList = new MediaItems[mis.size()];
        miList = mis.toArray(miList);
        mongoClient.close();
        return miList;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }
}