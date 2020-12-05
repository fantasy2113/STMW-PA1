import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class MyDOM {
    private final Set<Item> items = new HashSet<>();
    private final Set<User> users = new HashSet<>();
    private final Set<Bid> bids = new HashSet<>();
    private final Set<Location> locations = new HashSet<>();
    private final Set<Category> categories = new HashSet<>();
    private final Set<ItemCategory> itemsCategories = new HashSet<>();
    private final Set<String> bidders = new HashSet<>();

    public static void main(String[] args) {
        MyDOM myDOM = new MyDOM();

        List<String> files = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            files.add("ebay-data/items-{#}.xml".replace("{#}", String.valueOf(i)));
        }

        myDOM.run(files);
    }

    public void run(List<String> files) {
        System.out.println("MyDOM - Run: ");
        for (String file : files) {
            computeFile(file);
        }
        System.out.println();
        writeCsvFile(items);
        writeCsvFile(users);
        writeCsvFile(bids);
        writeCsvFile(locations);
        writeCsvFile(categories);
        writeCsvFile(itemsCategories);

        /*System.out.println();
        System.out.println("Items: " + items.size());
        System.out.println("Locations: " + locations.size());
        System.out.println("Users: " + users.size());
        System.out.println("Bids: " + bids.size());
        System.out.println("Categories: " + categories.size());
        System.out.println("ItemsCategories: " + itemsCategories.size());


        for (Bid bid : bids) {
            bidders.add(bid.user_id);
        }
        System.out.println("Bidders: " + bidders.size());
        int c = 0;
        for (Location location : locations) {
            if (location.latitude.length() > 0 | location.longitude.length() > 0) {
                c++;
            }
        }
        System.out.println(c);*/
    }

    private <T extends ICsvFile> void writeCsvFile(Iterable<T> data) {
        System.out.print(">");
        ICsvFile first = data.iterator().next();
        Path file = Paths.get(first.getFileName());
        List<String> lines = new ArrayList<>();
        lines.add(first.getHeaderLine());
        lines.add(first.toString());
        for (ICsvFile item : data) {
            lines.add(item.toString());
        }
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void computeFile(String file) {
        System.out.print("<");
        try {
            File fXmlFile = new File(file);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("Item");

            for (int itemIndex = 0; itemIndex < nodeList.getLength(); itemIndex++) {

                Node node = nodeList.item(itemIndex);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element itemElement = (Element) node;
                    Item item = mapToItem(itemElement);
                    items.add(item);
                    locations.add(mapToLocation(itemElement, item.id));
                    users.add(mapToUser(itemElement, item.user_id));
                    addBids(itemElement, item.id);
                    addCategories(itemElement, item.id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Item mapToItem(Element ele) {
        Element sellerEle = (Element) ele.getElementsByTagName("Seller").item(0);
        Item item = new Item();
        item.id = getValueAsInteger(ele, "ItemID");
        item.user_id = sellerEle.getAttribute("UserID");
        item.name = ele.getElementsByTagName("Name").item(0).getFirstChild().getNodeValue();
        item.currently = getValueAsDouble(ele, "Currently");
        item.first_bid = getValueAsDouble(ele.getElementsByTagName("First_Bid").item(0).getFirstChild().getNodeValue());
        item.number_of_bids = getValueAsInteger(ele.getElementsByTagName("Number_of_Bids").item(0).getFirstChild().getNodeValue());
        item.country = getValue(ele, "Country");
        item.started = getTimestampAsString(ele.getElementsByTagName("Started").item(0).getFirstChild().getNodeValue());
        item.ends = getTimestampAsString(ele.getElementsByTagName("Ends").item(0).getFirstChild().getNodeValue());
        item.description = getValue(ele, "Description");
        return item;
    }

    private Location mapToLocation(Element ele, long itemId) {
        Location location = new Location();
        NodeList location1 = ele.getElementsByTagName("Location");
        Element locationEle = (Element) location1.item(location1.getLength() - 1);
        location.item_id = itemId;
        location.latitude = locationEle.getAttribute("Latitude");
        location.longitude = locationEle.getAttribute("Longitude");
        location.place = locationEle.getFirstChild().getNodeValue();
        return location;
    }

    private User mapToUser(Element ele, String userId) {
        User user = new User();
        Element sellerEle = (Element) ele.getElementsByTagName("Seller").item(0);
        NodeList locations = ele.getElementsByTagName("Location");
        NodeList countries = ele.getElementsByTagName("Country");
        user.user_id = userId;
        user.rating = getValueAsInteger(sellerEle.getAttribute("Rating"));
        user.country = countries.item(countries.getLength() - 1).getFirstChild().getNodeValue();
        user.place = locations.item(locations.getLength() - 1).getFirstChild().getNodeValue();
        return user;
    }

    private void addBids(Element ele, long itemId) {
        NodeList nodes = ((Element) ele.getElementsByTagName("Bids").item(0)).getElementsByTagName("Bid");
        for (int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++) {
            Bid bid = new Bid();
            Element bidEle = (Element) nodes.item(nodeIndex);
            Element bidderEle = (Element) bidEle.getElementsByTagName("Bidder").item(0);
            bid.user_id = bidderEle.getAttribute("UserID");
            bid.item_id = itemId;
            bid.time = getTimestampAsString(bidEle.getElementsByTagName("Time").item(0).getFirstChild().getNodeValue());
            bid.amount = getValueAsDouble(bidEle.getElementsByTagName("Amount").item(0).getFirstChild().getNodeValue());
            bids.add(bid);
        }
    }

    private void addCategories(Element ele, long itemId) {
        NodeList categoryNodeList = ele.getElementsByTagName("Category");
        for (int i = 0; i < categoryNodeList.getLength(); i++) {
            String categoryName = categoryNodeList.item(i).getFirstChild().getNodeValue();
            Category category = new Category();
            category.name = categoryName;
            category.id = categoryName.hashCode();

            categories.add(category);

            ItemCategory itemCategory = new ItemCategory();
            itemCategory.item_id = itemId;
            itemCategory.category_id = category.id;

            itemsCategories.add(itemCategory);
        }
    }

    private String getValue(Element ele, String att) {
        try {
            return ele.getElementsByTagName(att).item(0).getFirstChild().getNodeValue();
        } catch (Exception ex) {
            return "";
        }
    }

    private double getValueAsDouble(Element element, String attribute) {
        String value = element.getAttribute(attribute);
        return (!Objects.equals(value, "")) ? Double.parseDouble(value
                .replace("$", "")
                .replace(",", "")) : 0;
    }

    private int getValueAsInteger(Element element, String attribute) {
        String value = element.getAttribute(attribute);
        return !Objects.equals(value, "") ? Integer.parseInt(value) : 0;
    }

    private double getValueAsDouble(String value) {
        return (!Objects.equals(value, "")) ? Double.parseDouble(value
                .replace("$", "")
                .replace(",", "")) : 0;
    }

    private int getValueAsInteger(String value) {
        return (!Objects.equals(value, "")) ? Integer.parseInt(value) : 0;
    }

    private String getTimestampAsString(String input) {
        try {
            DateFormat inFormatter = new SimpleDateFormat("MMM-dd-yy HH:mm:ss", Locale.ENGLISH);
            DateFormat outFormatter = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss aa", Locale.ENGLISH);
            Date date = inFormatter.parse(input);
            String output = outFormatter.format(date);
            return output;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

    private interface ICsvFile {
        String getHeaderLine();

        String getFileName();
    }

    private class Location implements ICsvFile {
        long item_id;
        String place = "";
        String latitude = "";
        String longitude = "";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Location location = (Location) o;
            return item_id == location.item_id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(item_id);
        }

        @Override
        public String toString() {
            return item_id + ";" + place + ";" + latitude + ";" + longitude;
        }

        @Override
        public String getHeaderLine() {
            return "item_id;place;latitude;longitude";
        }

        @Override
        public String getFileName() {
            return "location.csv";
        }
    }

    private class Item implements ICsvFile {
        long id;
        String user_id = "";
        String name = "";
        double currently;
        double first_bid;
        int number_of_bids;
        String country = "";
        String started = "";
        String ends = "";
        String description = "";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Item item = (Item) o;
            return id == item.id && Objects.equals(user_id, item.user_id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, user_id);
        }

        @Override
        public String toString() {
            return id + ";" + user_id + ";" + name + ";" + currently + ";" + first_bid + ";" + number_of_bids + ";" + country + ";" + started + ";" + ends + ";" + description;
        }

        @Override
        public String getHeaderLine() {
            return "id;user_id;name;currently;first_bid;number_of_bids;country;started;ends;description";
        }

        @Override
        public String getFileName() {
            return "items.csv";
        }
    }

    private class User implements ICsvFile {
        String user_id = "";
        int rating;
        String country = "";
        String place = "";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return Objects.equals(user_id, user.user_id) && Objects.equals(rating, user.rating) && Objects.equals(country, user.country) && Objects.equals(place, user.place);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user_id, rating, country, place);
        }

        @Override
        public String toString() {
            return user_id + ";" + rating + ";" + country + ";" + place;
        }

        @Override
        public String getHeaderLine() {
            return "user_id;rating;country;place";
        }

        @Override
        public String getFileName() {
            return "users.csv";
        }
    }

    private class Bid implements ICsvFile {
        String user_id = "";
        long item_id;
        String time = "";
        double amount;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Bid bid = (Bid) o;
            return Objects.equals(user_id, bid.user_id) && Objects.equals(item_id, bid.item_id) && Objects.equals(time, bid.time);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user_id, item_id, time);
        }

        @Override
        public String toString() {
            return user_id + ";" + item_id + ";" + time + ";" + amount;
        }

        @Override
        public String getHeaderLine() {
            return "user_id;item_id;time;amount";
        }

        @Override
        public String getFileName() {
            return "bids.csv";
        }
    }

    private class Category implements ICsvFile {
        long id;
        String name;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Category category = (Category) o;
            return id == category.id && Objects.equals(name, category.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }

        @Override
        public String toString() {
            return id + ";" + name;
        }

        @Override
        public String getHeaderLine() {
            return "id;name";
        }

        @Override
        public String getFileName() {
            return "categories.csv";
        }
    }

    private class ItemCategory implements ICsvFile {
        long item_id;
        long category_id;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemCategory that = (ItemCategory) o;
            return item_id == that.item_id && category_id == that.category_id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(item_id, category_id);
        }

        @Override
        public String toString() {
            return item_id + ";" + category_id;
        }

        @Override
        public String getHeaderLine() {
            return "item_id;category_id";
        }

        @Override
        public String getFileName() {
            return "items_categories.csv";
        }
    }
}
