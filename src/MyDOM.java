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
    private static final String FS_TAB = "\t";
    private final Set<Item> items = new HashSet<>();
    private final Set<User> users = new HashSet<>();
    private final Set<Bid> bids = new HashSet<>();
    private final Set<ItemLocation> locations = new HashSet<>();
    private final Set<Category> categories = new HashSet<>();
    private final Set<ItemCategory> itemsCategories = new HashSet<>();
    private final Set<Long> bidders = new HashSet<>();

    public static void main(String[] args) {
        MyDOM myDOM = new MyDOM();

        List<String> files = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            files.add("ebay_data/items-{#}.xml".replace("{#}", String.valueOf(i)));
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
        System.out.println();

        System.out.println();
        System.out.println("Items: " + items.size());
        System.out.println("ItemsLocations: " + locations.size());
        System.out.println("Users: " + users.size());
        System.out.println("Bids: " + bids.size());
        System.out.println("Categories: " + categories.size());
        System.out.println("ItemsCategories: " + itemsCategories.size());


        for (Bid bid : bids) {
            bidders.add(bid.user_id);
        }
        System.out.println("Bidders: " + bidders.size());
        int c = 0;
        for (ItemLocation location : locations) {
            if (location.latitude.length() > 0 | location.longitude.length() > 0) {
                c++;
            }
        }
        System.out.println(c);
    }

    private <T extends ICsvFile> void writeCsvFile(Iterable<T> data) {
        System.out.print(">");
        ICsvFile first = data.iterator().next();
        Path file = Paths.get(first.getFileName());
        List<String> lines = new ArrayList<>();
        lines.add(replaceFs(first.getHeaderLine()));
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
                    addBidsAndUser(itemElement, item.id);
                    users.add(mapToUser(itemElement, item.user_id_as_str));
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
        String user_name = sellerEle.getAttribute("UserID");
        item.user_id = Objects.hash(user_name);
        item.user_id_as_str = user_name;
        item.name = ele.getElementsByTagName("Name").item(0).getFirstChild().getNodeValue();
        item.currently = getValueAsDouble(ele.getElementsByTagName("Currently").item(0).getFirstChild().getNodeValue());
        item.first_bid = getValueAsDouble(ele.getElementsByTagName("First_Bid").item(0).getFirstChild().getNodeValue());
        item.number_of_bids = getValueAsInteger(ele.getElementsByTagName("Number_of_Bids").item(0).getFirstChild().getNodeValue());
        item.started = getTimestampAsString(ele.getElementsByTagName("Started").item(0).getFirstChild().getNodeValue());
        item.ends = getTimestampAsString(ele.getElementsByTagName("Ends").item(0).getFirstChild().getNodeValue());
        String description = getValue(ele, "Description");
        description = description.length() > 4000 ? description.substring(0, 3999) : description;
        item.description = description;
        return item;
    }

    private ItemLocation mapToLocation(Element ele, long itemId) {
        ItemLocation location = new ItemLocation();
        NodeList location1 = ele.getElementsByTagName("Location");
        Element locationEle = (Element) location1.item(location1.getLength() - 1);
        location.item_id = itemId;
        location.latitude = locationEle.getAttribute("Latitude");
        location.longitude = locationEle.getAttribute("Longitude");
        location.place = locationEle.getFirstChild().getNodeValue();
        return location;
    }

    private User mapToUser(Element ele, String userName) {
        User user = new User();
        Element sellerEle = (Element) ele.getElementsByTagName("Seller").item(0);
        NodeList locations = ele.getElementsByTagName("Location");
        NodeList countries = ele.getElementsByTagName("Country");
        user.id = Objects.hash(userName);
        user.name = userName;
        user.rating = getValueAsInteger(sellerEle.getAttribute("Rating"));
        user.country = countries.item(countries.getLength() - 1).getFirstChild().getNodeValue();
        user.place = locations.item(locations.getLength() - 1).getFirstChild().getNodeValue();
        return user;
    }

    private void addBidsAndUser(Element ele, long itemId) {
        NodeList nodes = ((Element) ele.getElementsByTagName("Bids").item(0)).getElementsByTagName("Bid");
        for (int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++) {
            Bid bid = new Bid();
            Element bidEle = (Element) nodes.item(nodeIndex);
            Element bidderEle = (Element) bidEle.getElementsByTagName("Bidder").item(0);
            bid.user_id = Objects.hash(bidderEle.getAttribute("UserID"));
            bid.item_id = itemId;
            bid.id = Objects.hash(bid.user_id, itemId);
            bid.time = getTimestampAsString(bidEle.getElementsByTagName("Time").item(0).getFirstChild().getNodeValue());
            bid.amount = getValueAsDouble(bidEle.getElementsByTagName("Amount").item(0).getFirstChild().getNodeValue());

            bids.add(bid);

            try {
                User user = new User();
                user.id = bid.user_id;
                user.name = bidderEle.getAttribute("UserID");
                user.rating = getValueAsInteger(bidderEle.getAttribute("Rating"));
                user.country = bidderEle.getElementsByTagName("Country").item(0).getFirstChild().getNodeValue();
                user.place = bidderEle.getElementsByTagName("Location").item(0).getFirstChild().getNodeValue();

                users.add(user);
            } catch (Exception ex) {
            }
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
            DateFormat outFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date date = inFormatter.parse(input);
            String output = outFormatter.format(date);
            return output;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

    public String replaceFs(String header) {
        return header.replace("{fs}", FS_TAB);
    }

    private interface ICsvFile {
        String getHeaderLine();

        String getFileName();
    }

    private class ItemLocation implements ICsvFile {
        long item_id;
        String place = "";
        String latitude = "";
        String longitude = "";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemLocation location = (ItemLocation) o;
            return item_id == location.item_id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(item_id);
        }

        @Override
        public String toString() {
            return item_id + FS_TAB + place + FS_TAB + latitude + FS_TAB + longitude;
        }

        @Override
        public String getHeaderLine() {
            return "item_id{fs}place{fs}latitude{fs}longitude";
        }

        @Override
        public String getFileName() {
            return "items_locations.csv";
        }
    }

    private class Item implements ICsvFile {
        long id;
        long user_id;
        String user_id_as_str;
        String name = "";
        double currently;
        double first_bid;
        int number_of_bids;
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
            return id + FS_TAB + user_id + FS_TAB + name + FS_TAB + currently + FS_TAB + first_bid + FS_TAB + number_of_bids + FS_TAB + started + FS_TAB + ends + FS_TAB + description;
        }

        @Override
        public String getHeaderLine() {
            return "id{fs}user_id{fs}name{fs}currently{fs}first_bid{fs}number_of_bids{fs}started{fs}ends{fs}description";
        }

        @Override
        public String getFileName() {
            return "items.csv";
        }
    }

    private class User implements ICsvFile {
        long id;
        String name = "";
        int rating;
        String country = "";
        String place = "";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return id == user.id && Objects.equals(name, user.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }

        @Override
        public String toString() {
            return id + FS_TAB + name + FS_TAB + rating + FS_TAB + country + FS_TAB + place;
        }

        @Override
        public String getHeaderLine() {
            return "id{fs}name{fs}rating{fs}country{fs}place";
        }

        @Override
        public String getFileName() {
            return "users.csv";
        }
    }

    private class Bid implements ICsvFile {
        long id;
        long user_id;
        long item_id;
        String time = "";
        double amount;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Bid bid = (Bid) o;
            return id == bid.id && item_id == bid.item_id && Objects.equals(user_id, bid.user_id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, user_id, item_id);
        }

        @Override
        public String toString() {
            return id + FS_TAB + user_id + FS_TAB + item_id + FS_TAB + time + FS_TAB + amount;
        }

        @Override
        public String getHeaderLine() {
            return "id{fs}user_id{fs}item_id{fs}time{fs}amount";
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
            return id + FS_TAB + name;
        }

        @Override
        public String getHeaderLine() {
            return "id{fs}name";
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
            return item_id + FS_TAB + category_id;
        }

        @Override
        public String getHeaderLine() {
            return "item_id{fs}category_id";
        }

        @Override
        public String getFileName() {
            return "items_categories.csv";
        }
    }
}
