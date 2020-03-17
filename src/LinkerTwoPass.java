import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class LinkerTwoPass {	
	public static void main(String[] args) {
		int numMods;
		int numDefs;
		int numNUPairs;
		HashMap<Integer, Integer> allModBaseAddrss = new HashMap<>();
		HashMap<Integer, Integer> allModLength = new HashMap<>();
		Map<String,Integer> symTable = new HashMap<>();
		Map<String,Integer> symTableMod = new HashMap<>();
		Map<String,String> symTableError = new HashMap<>();
		Map<String,Boolean> symTableUsed = new HashMap<>();
		HashMap<Integer,HashMap<Integer,String>> allNUPairs = new HashMap<>();
		HashMap<Integer,HashMap<String,Integer>> allNUPairsDefined = new HashMap<>();
		Map<String,String> nuPairLengthError = new HashMap<>();
		Map<String,String> extVarUsedInstrcnPlace = new HashMap<>();
		HashMap<Integer,List<Integer>> allNTDigits = new HashMap<>();
		HashMap<Integer,Integer> modNTLengths = new HashMap<>();
		
		
		Scanner sc = new Scanner(System.in);
		
		/**PASS ONE*/
		//save the total number of modules in the input
		numMods = sc.nextInt();
		
		//read through the input txt file until you reach the end of the file
		while (sc.hasNext()) {	
			//each iteration represents looking at a new module
			for (int i = 0; i < numMods; i ++) { 
				//COMPUTE CURRENT MODULE BASE ADDRESS
				if (i == 0) {
					allModBaseAddrss.put(i,0);
				} else {
					allModBaseAddrss.put(i,allModBaseAddrss.get(i-1)+allModLength.get(i-1));
				}
				
				
				//DEFINITION LIST
				//for all ND pairs, store into the symbol table
				numDefs = sc.nextInt();
				HashMap<String, Integer> symDefined = new HashMap<>();
				while (numDefs > 0) {
					String symbol = sc.next();
					int relativeAddrss = Integer.parseInt(sc.next());
					if (symTable.containsKey(symbol)) {
						symTable.replace(symbol, relativeAddrss + allModBaseAddrss.get(i));
						symTableError.put(symbol,"Error: This variable is multiply defined; last value used.");
					} else {
						symTable.put(symbol, relativeAddrss + allModBaseAddrss.get(i));
						symTableMod.put(symbol, i);//stores the external symbol and the module # defined in
						symTableError.put(symbol,"");
						symTableUsed.put(symbol,false); 
						symDefined.put(symbol, relativeAddrss);
					}			
					numDefs --;
					allNUPairsDefined.put(i,symDefined);
				}
				

				
				
				//USE LIST
				//store total number of NU pairs in the current module
				numNUPairs = sc.nextInt();
				//create a new HashMap variable, "currModNUPairs", that will store all NU pairs of the current module
				HashMap<Integer,String> currModNUPairs = new HashMap<>();
				//look at each NU pair
				while (numNUPairs > 0) {	
					//save the external symbol "S" used in the module
					String symbolUsed = sc.next();
					
					//save a list of relative addresses in the module in which the current external symbol is being used
					//List<Integer> usages = new ArrayList<>();
					int place = sc.nextInt();
					//keep adding the relative addresses until you reach -1
					//-1 signifies that you reached the end of the current NU pair
					while (place != -1) {
						//store the place where the external symbol is used and the symbol into the "currModNUPairs" HashMap
						if (currModNUPairs.containsKey(place)) {
							extVarUsedInstrcnPlace.put(symbolUsed, "Error: Multiple variables used in instruction; all but last ignored.");
						} if (!currModNUPairs.containsKey(place)) {
							extVarUsedInstrcnPlace.put(symbolUsed, "");
						}
						currModNUPairs.put(place,symbolUsed);
						place = sc.nextInt();
					}
					numNUPairs --;
				}
				allNUPairs.put(i,currModNUPairs);
					
				
				//PROGRAM TEXT
				//COMPUTE CURRENT MODULE LENGTH
				int numNTPairs = sc.nextInt(); //stands for the number of NT pairs each module has
				allModLength.put(i,numNTPairs);
								
				//store the number of NT pairs of each module
				modNTLengths.put(i, numNTPairs);
				
				//store all NT 5-digit numbers of the current module
				List<Integer> currNTs = new ArrayList<Integer>();
				int j = 0;
				while (j < numNTPairs) {
					currNTs.add(sc.nextInt());
					j ++;
				}
				allNTDigits.put(i,currNTs);		 
			} 
		}		
		
		System.out.println();
		
		//prints out the Symbol Table (prints an external and its absolute address where it was defined
		System.out.println("Symbol Table");
		if (symTable.size() != 0) {
			TreeMap<String, Integer> sorted = new TreeMap<>();
			sorted.putAll(symTable);
			for (Map.Entry<String,Integer> entry : sorted.entrySet()) {
				String key = entry.getKey();
				for (int jjj: allNUPairsDefined.keySet()) {
//					System.out.println("jjj: " + jjj);
//					System.out.println("allNUPairsDefined.get(jjj): " + allNUPairsDefined.get(jjj));
//					System.out.println("allNUPairsDefined.get(jjj).get(key): " + allNUPairsDefined.get(jjj).get(key));
//					System.out.println("modNTLengths.get(jjj): " + modNTLengths.get(jjj));
					if (allNUPairsDefined.get(jjj).get(key) != null) {
						if (allNUPairsDefined.get(jjj).get(key) >= modNTLengths.get(jjj)) {
							int newLength = allModBaseAddrss.get(jjj) + allModLength.get(jjj) - 1;
							//System.out.println("newLength: " + newLength);
							entry.setValue(newLength);
							symTable.put(key,newLength);
							nuPairLengthError.put(key, "Error: Definition exceeds module size; last word in module used.");
						} else {
							nuPairLengthError.put(key, "");	
						}
					}
				}
				System.out.println(key + "=" + entry.getValue() + " " + symTableError.get(key) + nuPairLengthError.get(key));
			} 			
		}
		System.out.println();

		
		/**PASS TWO*/
		System.out.println("Memory Map");
		int finalNTDigit;
		int ntCounter = 0;
		int index = 0;
		String temp;
		String extRef;
		for (int k = 0; k < numMods; k ++) {
			for (int m = 0; m < allNTDigits.get(k).size(); m ++) {
				int origNTDigit = allNTDigits.get(k).get(m);
				
				//turning relative address into absolute address
				if (origNTDigit % 10 == 3) {
					finalNTDigit = origNTDigit/10 + allModBaseAddrss.get(k);
					System.out.printf("%d:  %d\n",index,finalNTDigit);
				} 
				
				//resolving external reference
				else if (origNTDigit % 10 == 4) {
					temp = Integer.toString(origNTDigit/10000);
					if (symTable.get(allNUPairs.get(k).get(ntCounter)) == null) {
						System.out.printf("%d:  %s111 Error: %s is not defined; 111 used\n",index,temp,allNUPairs.get(k).get(ntCounter));
					} else {
						extRef = Integer.toString(symTable.get(allNUPairs.get(k).get(ntCounter)));
						int length = 3 - extRef.length();
						for (int n = 0; n < length; n ++) {
							extRef = "0" + extRef;
						}
						temp += extRef;
						finalNTDigit = Integer.parseInt(temp);
						System.out.printf("%d:  %d %s\n",index,finalNTDigit,extVarUsedInstrcnPlace.get(allNUPairs.get(k).get(ntCounter)));
						symTableUsed.put(allNUPairs.get(k).get(ntCounter),true);
					}
					
				} else if (origNTDigit % 10 == 2) {
					if (Integer.parseInt(Integer.toString(origNTDigit).substring(1,4)) > 299) {
						temp = origNTDigit/10000 + "299";
						System.out.printf("%d:  %s Error: Absolute address exceeds machine size; largest legal value used.\n",index,temp);
					} else {
						finalNTDigit = origNTDigit/10;
						System.out.printf("%d:  %d\n",index,finalNTDigit);
					}
				} else {
					finalNTDigit = origNTDigit/10;
					System.out.printf("%d:  %d\n",index,finalNTDigit);
				}
				ntCounter ++;
				index ++;
			}
			ntCounter = 0;
		}
		
		System.out.println();

		TreeMap<String, Boolean> symTabUsedSorted = new TreeMap<>();
		symTabUsedSorted.putAll(symTableUsed);
		for (Entry<String, Boolean> item : symTabUsedSorted.entrySet()) {
			if (!item.getValue()) {
				System.out.printf("Warning: %s was defined in module %d but never used.\n", item.getKey(), symTableMod.get(item.getKey()));
			}
		}
		
//		System.out.println("allModBaseAddrss: " + allModBaseAddrss);
//		System.out.println("allModLength: " + allModLength);
//		System.out.println("symTable: " + symTable);
//		System.out.println("symTableMod: " + symTableMod);
//		System.out.println("symTableUsed: " + symTableUsed);
//		System.out.println("allNUPairsDefined: " + allNUPairsDefined);
//		System.out.println("allNUPairs: " + allNUPairs);
//		System.out.println("allNTDigits: " + allNTDigits);		
	}

}
