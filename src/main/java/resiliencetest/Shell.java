package resiliencetest;

import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.SimpleCompletor;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Shell {
   String[] commands = {"help", "put", "get", "remove", "list", "exit", "fill", "size"};
   EmbeddedCacheManager embeddedCacheManager;
   Cache<String, String> cache;
   static Random random = new Random();

   public static void main(String[] args) throws IOException {
      Shell shell = new Shell();
      shell.init();
      shell.run();
   }

   private void init() throws IOException {
      // Start Infinispan
      embeddedCacheManager = new DefaultCacheManager("infinispan.xml");
      cache = embeddedCacheManager.getCache();
      System.out.println("Infinispan started.  Address: " + embeddedCacheManager.getAddress());
   }

   private void run() throws IOException {
      ConsoleReader reader = new ConsoleReader();
      reader.setBellEnabled(false);
      List<Completor> completors = new LinkedList<Completor>();

      completors.add(new SimpleCompletor(commands));
      reader.addCompletor(new ArgumentCompletor(completors));

      String line;
      PrintWriter out = new PrintWriter(System.out);

      while ((line = readLine(reader, "")) != null) {
         if ("help".equals(line)) {
            displayHelp();
         } else if (line.startsWith("put")) {
            String [] params = getParams(line);
            String old = cache.put(params[0], params[1]);
            System.out.printf("Stored %s under key %s, replacing old value %s%n", params[1], params[0], old);
         } else if (line.startsWith("get")) {
            String [] params = getParams(line);
            String old = cache.get(params[0]);
            System.out.printf("Value of %s is %s%n", params[0], old);
         } else if (line.startsWith("remove")) {
            String [] params = getParams(line);
            String old = cache.remove(params[0]);
            System.out.printf("Removed entry %s, old value was %s%n", params[0], old);
         } else if (line.startsWith("fill")) {
            String [] params = getParams(line);
            int last = fill(params);
            System.out.printf("Created entries key0 through to key%s%n", last);
         } else if (line.startsWith("size")) {
            System.out.printf("Local node contains %s entries%n", cache.size());
         } else if ("list".equals(line)) {
            Set<String> keys = cache.keySet();
            System.out.println("Cache contains the following keys: " + keys);
         }  else if ("exit".equals(line)) {
            System.out.println("Exiting cleanly ... ");
            embeddedCacheManager.stop();
            return;
         } else {
            System.out
                  .println("Invalid command, For assistance press TAB or type \"help\" then hit ENTER.");
         }
         out.flush();
      }
   }
   
   private int fill(String[] params) {
      int numEntries = Integer.parseInt(params[0]);
      int size = Integer.parseInt(params[1]);
      for (int i=0; i<numEntries; i++) cache.put("key" + i, generate(size));
      return numEntries - 1;
   }
   
   private String generate(int numBytes) {
      StringBuilder sb = new StringBuilder();
      for (int i=0; i<numBytes; i++) sb.append((char) (random.nextInt(26) + 'A'));
      return sb.toString();
   }

   private String[] getParams(String line) {
      String[] s = line.split(" ");
      if (s.length == 1) return new String[]{};
      String[] params = new String[s.length - 1];
      System.arraycopy(s, 1, params, 0, s.length - 1);
      return params;
   }
   
   private String readLine(ConsoleReader reader, String promtMessage)
         throws IOException {
      String line = reader.readLine(promtMessage + "\nInfinispan> ");
      return line.trim();
   }

   private void displayHelp() {
      System.out.println("Infinispan test shell");
      System.out.println("---------------------\n");
      System.out.println("Valid commands:");
      System.out.println("  put <key> <value>             - stores an entry");
      System.out.println("  get <key>                     - retrieves an entry");
      System.out.println("  remove <key>                  - removes an entry");
      System.out.println("  list                          - lists entries locally stored");
      System.out.println("  size                          - counts entries locally stored");
      System.out.println("  fill <numEntries> <entrySize> - generates numEntries, each of entrySize bytes");
      System.out.println("  exit                          - Quit cleanly");
      System.out.println("  help                          - prints this message\n\n");
   }
}
