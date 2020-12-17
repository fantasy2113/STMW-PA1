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
  public String rootOutPath = "";

  public MyDOM() {
  }

  public MyDOM(String rootOutPath) {
    this.rootOutPath = rootOutPath;
  }

  private static List<String> getFilePaths() {
    String ebayData = System.getenv("EBAY_DATA");
    List<String> paths = new ArrayList<>();
    for (int i = 0; i < 40; i++) {
      paths.add(ebayData + "/items-{#}.xml".replace("{#}", String.valueOf(i)));
    }
    return paths;
  }

  public static void main(String... args) {
    new MyDOM(args.length > 0 ? args[0] : "").run(getFilePaths());
  }

  public void run(List<String> files) {
    System.out.println();
    System.out.println("MyDOM:\tStart");
    System.out.println("\tFiles loaded:\t" + System.getenv("EBAY_DATA"));
    System.out.print("\tComputing:\t\t[");
    for (String file : files) {
      loadXMLFile(file);
    }
    System.out.print("]");
    System.out.println();
    System.out.print("\tWriting:\t\t\t[");
    writeCSVFile(items);
    writeCSVFile(users);
    writeCSVFile(bids);
    writeCSVFile(locations);
    writeCSVFile(categories);
    writeCSVFile(itemsCategories);
    System.out.print("]");
    System.out.println();
    System.out.println("MyDOM:\tStop");
  }

  private void loadXMLFile(String file) {
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
          if (items.add(item)) {
            locations.add(mapToLocation(itemElement, item.id));
            users.add(mapToUser(itemElement, item));
            addBidsAndUser(itemElement, item);
            addCategories(itemElement, item.id);
          }
        }
      }
      System.out.print("<");
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private <T extends ICSVFile> void writeCSVFile(Iterable<T> data) {
    ICSVFile first = data.iterator().next();
    Path file = Paths.get(rootOutPath + first.getFileName());
    List<String> lines = new ArrayList<>();
    lines.add(replaceFs(first.getHeaderLine()));
    for (ICSVFile item : data) {
      lines.add(item.toString());
    }
    try {
      Files.write(file, lines, StandardCharsets.UTF_8);
      System.out.print(">");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Item mapToItem(Element ele) {
    Element sellerEle = (Element) ele.getElementsByTagName("Seller").item(0);
    Item item = new Item();
    item.id = getValueAsInteger(ele, "ItemID");
    String user_name = sellerEle.getAttribute("UserID").trim();
    item.user_id = Objects.hash(user_name, getValueAsInteger(sellerEle.getAttribute("Rating")));
    item.user_id_as_str = user_name;
    item.name = ele.getElementsByTagName("Name").item(0).getFirstChild().getNodeValue();
    item.currently = getValueAsDouble(ele.getElementsByTagName("Currently").item(0).getFirstChild().getNodeValue());
    item.first_bid = getValueAsDouble(ele.getElementsByTagName("First_Bid").item(0).getFirstChild().getNodeValue());
    item.number_of_bids = getValueAsInteger(ele.getElementsByTagName("Number_of_Bids").item(0).getFirstChild().getNodeValue());
    try {
      item.buy_price = getValueAsDouble(ele.getElementsByTagName("Buy_Price").item(0).getFirstChild().getNodeValue());
    } catch (Exception e) {
    }
    item.started = getTimestampAsString(ele.getElementsByTagName("Started").item(0).getFirstChild().getNodeValue());
    item.ends = getTimestampAsString(ele.getElementsByTagName("Ends").item(0).getFirstChild().getNodeValue());
    String description = getValue(ele, "Description");
    item.description = description.length() > 4000 ? description.substring(0, 3999) : description;
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

  private User mapToUser(Element ele, Item item) {
    User user = new User();
    Element sellerEle = (Element) ele.getElementsByTagName("Seller").item(0);
    NodeList locations = ele.getElementsByTagName("Location");
    NodeList countries = ele.getElementsByTagName("Country");
    user.id = item.user_id;
    user.name = item.user_id_as_str;
    user.rating = getValueAsInteger(sellerEle.getAttribute("Rating"));
    user.country = countries.item(countries.getLength() - 1).getFirstChild().getNodeValue();
    user.place = locations.item(locations.getLength() - 1).getFirstChild().getNodeValue();
    return user;
  }

  private void addBidsAndUser(Element ele, Item item) {
    NodeList nodes = ((Element) ele.getElementsByTagName("Bids").item(0)).getElementsByTagName("Bid");
    for (int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++) {
      Bid bid = new Bid();
      Element bidEle = (Element) nodes.item(nodeIndex);
      Element bidderEle = (Element) bidEle.getElementsByTagName("Bidder").item(0);
      bid.user_id = Objects.hash(bidderEle.getAttribute("UserID").trim(), getValueAsInteger(bidderEle.getAttribute("Rating")));
      bid.item_id = item.id;
      bid.id = Objects.hash(bid.user_id, item.id);
      bid.time = getTimestampAsString(bidEle.getElementsByTagName("Time").item(0).getFirstChild().getNodeValue());
      bid.amount = getValueAsDouble(bidEle.getElementsByTagName("Amount").item(0).getFirstChild().getNodeValue());

      if (bids.add(bid)) {
        User user = new User();
        user.id = bid.user_id;
        user.name = bidderEle.getAttribute("UserID").trim();
        user.rating = getValueAsInteger(bidderEle.getAttribute("Rating"));
        try {
          user.country = bidderEle.getElementsByTagName("Country").item(0).getFirstChild().getNodeValue();
          user.place = bidderEle.getElementsByTagName("Location").item(0).getFirstChild().getNodeValue();
        } catch (Exception e) {
        }
        users.add(user);
      }
    }
  }

  private void addCategories(Element ele, long itemId) {
    NodeList categoryNodeList = ele.getElementsByTagName("Category");
    for (int i = 0; i < categoryNodeList.getLength(); i++) {
      String categoryName = categoryNodeList.item(i).getFirstChild().getNodeValue();
      Category category = new Category();
      category.name = categoryName.trim();
      category.id = Objects.hash(category.name);
      categories.add(category);

      ItemCategory itemCategory = new ItemCategory();
      itemCategory.item_id = itemId;
      itemCategory.category_id = category.id;
      itemsCategories.add(itemCategory);
    }
  }

  private String getValue(Element ele, String attribute) {
    try {
      return ele.getElementsByTagName(attribute).item(0).getFirstChild().getNodeValue();
    } catch (Exception e) {
      return "";
    }
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
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        .format(new SimpleDateFormat("MMM-dd-yy HH:mm:ss", Locale.ENGLISH)
          .parse(input));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return input;
  }

  public String replaceFs(String header) {
    return header.replace("{fs}", FS_TAB);
  }

  private interface ICSVFile {
    String getHeaderLine();

    String getFileName();
  }

  private class ItemLocation implements ICSVFile {
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

  private class Item implements ICSVFile {
    long id;
    long user_id;
    String user_id_as_str;
    String name = "";
    double currently;
    double first_bid;
    int number_of_bids;
    double buy_price;
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

    @Override
    public String toString() {
      return id + FS_TAB + user_id + FS_TAB + name + FS_TAB + currently + FS_TAB + first_bid + FS_TAB + number_of_bids + FS_TAB + buy_price + FS_TAB + started + FS_TAB + ends + FS_TAB + description;
    }

    @Override
    public String getHeaderLine() {
      return "id{fs}user_id{fs}name{fs}currently{fs}first_bid{fs}number_of_bids{fs}buy_price{fs}started{fs}ends{fs}description";
    }

    @Override
    public String getFileName() {
      return "items.csv";
    }
  }

  private class User implements ICSVFile {
    long id;
    String name = "";
    int rating;
    String country = "";
    String place = "";

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      User bidder = (User) o;
      return Objects.equals(name, bidder.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
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

  private class Bid implements ICSVFile {
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
      return user_id == bid.user_id && item_id == bid.item_id;
    }

    @Override
    public int hashCode() {
      return Objects.hash(user_id, item_id);
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

  private class Category implements ICSVFile {
    long id;
    String name;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Category category = (Category) o;
      return id == category.id;
    }

    @Override
    public int hashCode() {
      return Objects.hash(id);
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

  private class ItemCategory implements ICSVFile {
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
