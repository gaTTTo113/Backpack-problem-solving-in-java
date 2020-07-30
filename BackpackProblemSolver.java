import java.io.*;
import java.util.Random;
import java.util.ArrayList;


class Item {
    private final int value;
    private final int weight;
    public Item(int value, int weight) {
        this.value = value;
        this.weight = weight;
    }
    public int getWeight() {return weight;}
    public int getValue() {return value;}
}

class ItemChooser<E> implements Serializable, Cloneable{
    public int numberOfItems;
    public boolean[] solution;
    public int fitness;
    public int neighborhoodsStructure;
    public int cap;
    ArrayList<E> itemList;
    public int totalWeight;

    public ItemChooser(int numberOfItems, int sizeOfTheNeighbourhood, int capacity, ArrayList<E> items, boolean... b){
        this.numberOfItems = numberOfItems;
        solution = new boolean[numberOfItems];
        if (b == null)
            for (int i = 0; i< numberOfItems; i++)
                solution[i] = false;
        else
            solution = b;

        neighborhoodsStructure = sizeOfTheNeighbourhood;
        cap = capacity;
        itemList = items;
        fitness = calculateTotalProfit();
        totalWeight = calculateWeight();
    }

    @Override
    public Object clone(){
        try{
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return new ItemChooser<E>(this.numberOfItems, this.neighborhoodsStructure, this.cap, this.itemList, this.solution);
        }
    }

    public ItemChooser<E> findNeighbour(){
        Random r = new Random();
        int howManyToChange = r.nextInt(neighborhoodsStructure)+1;
        int randItem;

        ArrayList<Integer> nonTakenItemsID = getNonTakenItems();
        boolean[] solutionCopy = new boolean[solution.length];
        // not to do a shallow copy
        System.arraycopy(this.solution, 0, solutionCopy, 0, this.solution.length);
        ItemChooser<E> neighbor =  new ItemChooser<E>(numberOfItems, neighborhoodsStructure, cap, itemList, solutionCopy);

        while(howManyToChange-- !=0){

            randItem = r.nextInt(nonTakenItemsID.size());

            neighbor.solution[nonTakenItemsID.get(randItem)] = true;
            neighbor.calculateWeight();

            if (neighbor.isOverWeight()){// if you are over-weight drop item
                ArrayList<Integer> takenItemsID = neighbor.getTakenItems();
                do {
                    randItem = r.nextInt(takenItemsID.size());

                    neighbor.solution[takenItemsID.get(randItem)] = false;
                    neighbor.calculateWeight();
                }while (neighbor.isOverWeight());
            }
        }
        return neighbor;
    }

    public boolean isOverWeight(){
        return (totalWeight > cap);
    }

    public int calculateTotalProfit(){
        int totalProfit = 0;
        for(int i = 0; i<numberOfItems;i++){
            if (solution[i]) { // if item i is taken
                totalProfit += ((Item)itemList.get(i)).getValue();
            }
        }
        return totalProfit;
    }

    public int calculateWeight(){
        int sum=0;
        for(int i = 0; i<numberOfItems;i++){
            if (solution[i]) { // if item i is taken
                sum += ((Item)itemList.get(i)).getWeight();
            }
        }
        totalWeight = sum;
        return sum;
    }


    public ArrayList<Integer> getTakenItems(){
        ArrayList<Integer> a = new ArrayList<Integer>();
        for(int i = 0; i < solution.length; i++) {
            if (solution[i]) {
                a.add(i);
            }
        }
        return a;
    }

    public ArrayList<Integer> getNonTakenItems(){
        ArrayList<Integer> a = new ArrayList<Integer>();
        for(int i = 0; i < solution.length; i++)
            if (!solution[i])
                a.add(i);

        return a;
    }
}


public class BackpackProblemSolver {
    private static int n ;
    private static int k;
    private static double delta ;
    public static ArrayList<Item> items = new ArrayList<>();
    private static  double T;

    public static boolean isNeighbourAccepted(ItemChooser<Item> current, ItemChooser<Item> neighbor, double temperature){
        int delta = current.fitness - neighbor.fitness;
        if  (delta<0)
            return true;
        return Math.random() < Math.exp(-1*delta/temperature);
    }

    public static void main(String [ ] args) throws IOException {
        BufferedReader bfr = new BufferedReader(new FileReader(args[0]));
        String[] line = bfr.readLine().split(" ");
        if (line.length != 0) {
            k = Integer.parseInt(line[0]);
            n = Integer.parseInt(line[1]);
            T = Double.parseDouble(line[2]);
            delta = Integer.parseInt(line[3]);
        }

        System.out.println("k = " + k + " n = " + n  + " T = " + T + " delta = " + delta );
        String[] values = bfr.readLine().split(",");
        String[] weights = bfr.readLine().split(",");
        if (weights.length == values.length) {
            for (int i = 0; i < weights.length; i++) {
                items.add(new Item(Integer.parseInt(values[i]), Integer.parseInt(weights[i])));
            }
        }

        ItemChooser<Item> current = new ItemChooser<>(n,1, k, items, null);
        ArrayList<Integer> solutionKey = current.getTakenItems();
        int bestFitness = current.fitness;
        while (T-delta > 0){
            int neighborhoodStructure = 4;
            ItemChooser<Item> neighbor = new ItemChooser<>
                    (n, neighborhoodStructure, k, items, current.findNeighbour().solution);
            if (isNeighbourAccepted(current, neighbor, T)) {
                current = neighbor;
            }
            T -= delta;

            if (bestFitness < current.fitness){
                bestFitness = current.fitness;
                solutionKey = current.getTakenItems();
            }

            System.out.println(current.getTakenItems() + " T = " +  T);
        }

        // ANSWER --- ----- ---
        System.out.println( "founded answer: total_value = " + current.fitness + " " + current.getTakenItems());
        System.out.println( "best answer: total_value = " + bestFitness + " " + solutionKey);
        int j = 0;
        for (int i = 0; i < n; i++) {
            try {
                if (current.getTakenItems().get(j) == i) {
                    j++;
                    System.out.print(1);
                } else {
                    System.out.print(0);
                }
            }catch (Exception e){
                System.out.println(0);
            }
        }
        System.out.println();
    }
}
