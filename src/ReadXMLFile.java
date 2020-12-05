import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReadXMLFile {
    private final List<Item> items = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private final List<Bid> bids = new ArrayList<>();
    private final List<Location> locations = new ArrayList<>();

    public static void main(String[] args) {
        ReadXMLFile readXMLFile = new ReadXMLFile();

        List<String> files = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            files.add("ebay-data/items-{#}.xml".replace("{#}", String.valueOf(i)));
        }

        readXMLFile.computeFiles(files);
    }

    public void computeFiles(List<String> files) {
        for (String file : files) {
            computeFile(file);
        }
        System.out.println("Items: " + items.size());
        System.out.println("Locations: " + locations.size());
        System.out.println("Users: " + users.size());
        System.out.println("Bids: " + bids.size());
    }

    private void computeFile(String file) {
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
                    if (!items.contains(item)) {
                        items.add(item);
                        isAddLocation(mapToLocation(itemElement, item.id));
                        addBids(itemElement, item.id);
                    }
                    isAddUser(mapToUser(itemElement));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Item mapToItem(Element element) {
        Item item = new Item();
        item.id = getValueAsInteger(element, "ItemID");
        item.name = getValueAsString(element, "Name");
        item.currently = getValueAsDouble(element, "Currently");
        item.first_did = getValueAsDouble(element, "First_Bid");
        item.number_of_bids = getValueAsInteger(element, "Number_of_Bids");
        item.country = getValueAsString(element, "Country");
        item.started = getValueAsString(element, "Started");
        item.ends = getValueAsString(element, "Ends");
        item.description = getValueAsString(element, "Description");
        return item;
    }

    private Location mapToLocation(Element element, long itemId) {
        Location location = new Location();
        Element locationElement = (Element) element.getElementsByTagName("Location").item(0);
        location.item_id = itemId;
        location.latitude = getValueAsString(locationElement, "Latitude");
        location.longitude = getValueAsString(locationElement, "Longitude");
        return location;
    }

    private User mapToUser(Element element) {
        User user = new User();
        Element seller = (Element) element.getElementsByTagName("Seller").item(0);
        user.user_id = getValueAsString(seller, "UserID");
        user.rating = getValueAsString(seller, "Rating");
        user.country = getValueAsString(element, "Country");
        return user;
    }

    private void addBids(Element element, long itemId) {
        NodeList bidsNodes = element.getElementsByTagName("Bids");
        for (int i = 0; i < bidsNodes.getLength(); i++) {

        }
    }

    private boolean isAddLocation(Location location) {
        if (!locations.contains(location)) {
            locations.add(location);
            return true;
        }
        return false;
    }

    private boolean isAddItem(Item item) {
        if (!items.contains(item)) {
            items.add(item);
            return true;
        }
        return false;
    }

    private boolean isAddBid(Bid bid) {
        if (!bids.contains(bid)) {
            bids.add(bid);
            return true;
        }
        return false;
    }

    private boolean isAddUser(User user) {
        if (!users.contains(user)) {
            users.add(user);
            return true;
        }
        return false;
    }

    private String getValueAsString(Element element, String attribute) {
        String value = element.getAttribute(attribute);
        return value != null ? value : "";
    }

    private int getValueAsInteger(Element element, String attribute) {
        String value = element.getAttribute(attribute);
        return !Objects.equals(value, "") ? Integer.parseInt(value) : 0;
    }

    private double getValueAsDouble(Element element, String attribute) {
        String value = element.getAttribute(attribute);
        return (!Objects.equals(value, "")) ? Double.parseDouble(value.replace("$", "")) : 0;
    }

    private class Location {
        long item_id;
        String place = "";
        String latitude;
        String longitude;

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
    }

    private class Item {
        long id = 0L;
        String name = "";
        double currently;
        double first_did;
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
            return id == item.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    private class User {
        String user_id = "";
        String rating = "";
        String country = "";
        String place = "";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return Objects.equals(user_id, user.user_id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user_id);
        }
    }

    private class Bid {
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
    }

}
