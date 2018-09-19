import java.util.Random;

public class Network {
	
	//Horizontal number of nodes (max x).
	private int m;
	//Vertical number of nodes (max y).
	private int n;
	private double nodeSpacing;
	private double lossConstant;
	private double energyTotalGenerated;
	private double energyTotalRequired;
	private double generationCap;
	private double requirementCap;
	private double generationFloor;
	private Random rnd;
	private Random rnd1;
	
	private Node[][] nodeArray = null;
	private int[][] nodeRankArray = null;
	
	public Node[][] getNodeArray() {
		return nodeArray;
	}

	public void setNodeArray(Node[][] nodeArray) {
		this.nodeArray = nodeArray;
	}

	public Network(int horizontalNumberOfNodes, int verticalNumberOfNodes, double nodeSpacing, double lossConstant, 
			double energyTotalGenerated, double energyTotalRequired) {
		m = horizontalNumberOfNodes;
		n = verticalNumberOfNodes;
		this.nodeSpacing = nodeSpacing;
		this.lossConstant = lossConstant;
		this.energyTotalGenerated = energyTotalGenerated;
		this.energyTotalRequired = energyTotalRequired;
		rnd = new Random(System.currentTimeMillis());
		rnd1 = new Random(System.currentTimeMillis());
		generationCap = this.energyTotalGenerated;
		requirementCap = this.energyTotalRequired;
		generationFloor = requirementCap * 0.3;
		generateNodes();
	}
	
	public int getHorizontalNumberOfNodes() {
		return this.m;
	}
	
	public int getVerticalNumberOfNodes() {
		return this.n;
	}
	
	private void swapEnergies(Node a, Node b) {
		double temp;
		//Swap energy generation values.
		temp = a.getEnergyGenerated();
		a.setEnergyGenerated(b.getEnergyGenerated());
		b.setEnergyGenerated(temp);
		//Swap energy requirement values.
		temp = a.getEnergyRequired();
		a.setEnergyRequired(b.getEnergyRequired());
		b.setEnergyRequired(temp);
	}
	
	private void shuffleNodeEnergies() {
		for (int x=0; x < m; x++) {
			for (int y=0; y < n; y++) {
				int x1 = (int) ((m - 1) * rnd1.nextDouble());
				int y1 = (int) ((n - 1) * rnd1.nextDouble());
				swapEnergies(nodeArray[x][y], nodeArray[x1][y1]);
			}
		}
	}
	
	public void generateNodes(){
		if (nodeArray == null) {
			//Initialise arrays.
			nodeArray = new Node[m][n];
			nodeRankArray = new int[m][n];
			for (int x=0; x < m; x++) {
				for (int y=0; y < n; y++) {
					nodeArray[x][y] = new Node();
				}
			}
		}
		else {
			//Reset nodes in nodeArray.
			for (int x=0; x < m; x++) {
				for (int y=0; y < n; y++) {
					nodeArray[x][y].resetNode();
				}
			}
		}
		double energyGeneration;
		double energyGenRange = generationCap - generationFloor;
		//double energyDemand;
		//Perform random distribution of energy generation and demand.
		for (int x=0; x < m; x++) {
			for (int y=0; y < n; y++) {
				//Produce random energy generated value.
				energyGeneration = energyGenRange * rnd.nextDouble() + generationFloor;
				nodeArray[x][y].setEnergyGenerated(energyGeneration);
				//Produce random energy demand value.
				nodeArray[x][y].setEnergyRequired(requirementCap * rnd.nextDouble());
			}
		}
		//Shuffle the node energies to further increase randomness.
		shuffleNodeEnergies();
		
		/*for (int i=0; i < nodeList.size(); i++) {
			if (i != nodeList.size() - 1) {
				//Produce random fraction of generated energy.
				fractionGenerated = rnd.nextDouble() * remainingFractionGenerated;
				nodeList.get(i).setEnergyGenerated(fractionGenerated * energyTotalGenerated);
				remainingFractionGenerated -= fractionGenerated;
				//Produce random fraction of required energy.
				fractionRequired = rnd.nextDouble() * remainingFractionRequired;
				nodeList.get(i).setEnergyRequired(fractionRequired * energyTotalRequired);
				remainingFractionRequired -= fractionRequired;
			}
			else {
				//Assign remaining fraction of generated energy.
				nodeList.get(i).setEnergyGenerated(remainingFractionGenerated * energyTotalGenerated);
				remainingFractionGenerated = 1.0;
				//Assign remaining fraction of required energy.
				nodeList.get(i).setEnergyRequired(remainingFractionRequired * energyTotalRequired);
				remainingFractionRequired = 1.0;
			}
		}*/
	}
	
	public void redistributeEnergy() {
		boolean deficitExists = false;
		boolean transferOccurred = false;
		do {
			deficitExists = false;
			transferOccurred = false;
			for (int x=0; x < m; x++) {
				for (int y=0; y < n; y++) {
					Node s = nodeArray[x][y];
					
					if (s.hasEnergyDeficit()) {
						deficitExists = true;
					}
					
					boolean targetFound = false;
					if (s.hasEnergySurplus()) {
						//Reset all elements to rank 0.
						for (int x1=0; x1 < m; x1++) {
							for (int y1=0; y1 < n; y1++) {
								nodeRankArray[x1][y1] = 0;
							}
						}
						
						for (int x1=0; x1 < m; x1++) {
							for (int y1=0; y1 < n; y1++) {
								Node t = nodeArray[x1][y1];
								if (t.hasEnergyDeficit() && s.getEnergyNet() * 
										Math.pow(lossConstant, nodeSpacing * (Math.abs(x1 - x) + Math.abs(y1 - y))) + 
										t.getEnergyNet() >= 0) {
									nodeRankArray[x1][y1] = 1;
									targetFound = true;
								}
							}
						}
						if (targetFound) {
							System.out.println("Got here.");
							//In order to exit both loops when required, a boolean "eject" variable is used.
							boolean eject = false;
							//As two values are required for coordinate description,
							//closestIndex is now a two-element array.
							int[] closestIndex = new int[2];
							//Initialise closestIndex to first node with rank 1.
							for (int x1=0; x1 < m; x1++) {
								for (int y1=0; y1 < n; y1++) {
									if (nodeRankArray[x1][y1] == 1) {
										closestIndex[0] = x1;
										closestIndex[1] = y1;
										eject = true;
										break;
									}
								}
								if (eject) {
									break;
								}
							}
							//Find the closest potential target node to the supplier node.
							for (int x1=0; x1 < m; x1++) {
								for (int y1=0; y1 < n; y1++) {
									if (nodeRankArray[x1][y1] == 1) {
										if ((Math.abs(x1 - x) + Math.abs(y1 - y)) 
												< (Math.abs(closestIndex[0] - x) + Math.abs(closestIndex[1] - y))) {
											closestIndex[0] = x1;
											closestIndex[1] = y1;
										}
										else if (((Math.abs(x1 - x) + Math.abs(y1 - y)) 
												== (Math.abs(closestIndex[0] - x) + Math.abs(closestIndex[1] - y)))
												&& (nodeArray[x1][y1].getEnergyNet() 
												< nodeArray[closestIndex[0]][closestIndex[1]].getEnergyNet())) {
											closestIndex[0] = x1;
											closestIndex[1] = y1;
										}
									}
								}
							}
							nodeArray[closestIndex[0]][closestIndex[1]].importEnergy(s.exportEnergy() 
									* Math.pow(lossConstant, nodeSpacing * (Math.abs(closestIndex[0] - x) + Math.abs(closestIndex[1] - y))));
							
							transferOccurred = true;
							System.out.println("Supplier index = " + x + ", " + y);
							System.out.println("Target index = " + closestIndex[0] + ", " + closestIndex[1] + " rank = " 
									+ nodeRankArray[closestIndex[0]][closestIndex[1]]);
							System.out.println("Energy net = " + nodeArray[closestIndex[0]][closestIndex[1]].getEnergyNet());
							
						}
					}
				}
			}
		} while (transferOccurred);
		
		if (deficitExists) {
			for (int x=0; x < m; x++) {
				for (int y=0; y < n; y++) {
					Node t = nodeArray[x][y];
					
					int count = 1;
					boolean upperXBoundReached = false;
					boolean upperYBoundReached = false;
					boolean lowerXBoundReached = false;
					boolean lowerYBoundReached = false;
					while (t.hasEnergyDeficit() && !(upperXBoundReached && lowerXBoundReached 
							&& upperYBoundReached && lowerYBoundReached)) {
						System.out.println("Got here too.");
						//Move through nodes on sides with constant y.
						for (int x1 = x - count; x1 <= x + count; x1++) {
							if (t.hasEnergyDeficit()) {
								if (x1 < m) {
									if (x1 >= 0){
										if (y - count >= 0) {
											t.importEnergy(nodeArray[x1][y - count].exportEnergy() 
													* Math.pow(lossConstant, nodeSpacing * (count + Math.abs(x1 - x))));
										}
										if (t.hasEnergyDeficit() && y + count < n) {
											t.importEnergy(nodeArray[x1][y + count].exportEnergy() 
													* Math.pow(lossConstant, nodeSpacing * (count + Math.abs(x1 - x))));
										}
									}
									else {
										lowerXBoundReached = true;
									}
								}
								else {
									upperXBoundReached = true;
								}
							}
							else {
								break;
							}
						}
						//Move through nodes on sides with constant x.
						//+/-1 is used so there isn't overlap with the constant y sides.
						for (int y1 = y - count + 1; y1 <= y + count - 1; y1++) {
							if (t.hasEnergyDeficit()) {
								if (y1 < n) {
									if (y1 >= 0){
										if (x - count >= 0) {
											t.importEnergy(nodeArray[x - count][y1].exportEnergy() 
													* Math.pow(lossConstant, nodeSpacing * (count + Math.abs(y1 - y))));
										}
										if (t.hasEnergyDeficit() && x + count < m) {
											t.importEnergy(nodeArray[x + count][y1].exportEnergy() 
													* Math.pow(lossConstant, nodeSpacing * (count + Math.abs(y1 - y))));
										}
									}
									else {
										lowerYBoundReached = true;
									}
								}
								else {
									upperYBoundReached = true;
								}
							}
							else {
								break;
							}
						}
						++count;
						System.out.println("" + upperXBoundReached + upperYBoundReached + lowerXBoundReached + lowerYBoundReached);
					}
				}
			}
		}
		
		
		/*for (int i=0; i < nodeList.size(); i++) {
			Node n = nodeList.get(i);
			
			//If a node has no surplus or an insufficient surplus to meet the deficit of the
			//target node, then it is given rank 0.
			//If a node has a sufficient surplus, it is given a rank 1.
			boolean supplierFound = false;
			if (n.hasEnergyDeficit()) {
				for (int j=0; j < nodeRankList.size(); j++) {
					nodeRankList.set(j, 0);
				}
				
				for (int a=0; a < nodeRankList.size(); a++) {
					Node s = nodeList.get(a);
					if (s.getEnergyNet() * Math.pow(lossConstant, nodeSpacing * Math.abs(a - i)) + 
							n.getEnergyNet() >= 0) {
						nodeRankList.set(a, 1);
						supplierFound = true;
					}
				}
				if (supplierFound) {
					System.out.println("Got here.");
					int closestIndex = 0;
					//Initialise closestIndex to first node with rank 1.
					for (int a=0; a < nodeRankList.size(); a++) {
						if (nodeRankList.get(a) == 1) {
							closestIndex = a;
							break;
						}
					}
					//Find closest potential supplier node to target node.
					for (int a=0; a < nodeRankList.size(); a++) {
						if (nodeRankList.get(a) == 1) {
							if (Math.abs(a - i) < Math.abs(closestIndex - i)) {
								closestIndex = a;
							}
						}
					}
					n.importEnergy(nodeList.get(closestIndex).exportEnergy() * Math.pow(lossConstant, nodeSpacing * Math.abs(closestIndex - i)));
					System.out.println("Target index = " + i);
					System.out.println("Supplier index = " + closestIndex + " rank = " + nodeRankList.get(closestIndex));
					System.out.println("Energy net = " + n.getEnergyNet());
				}
			}*/
			
			
			
			/*for (int r=0; r < nodeList.size(); r++) {
				if (n.hasEnergyDeficit()) {
					if (r != i) {
						n.importEnergy(nodeList.get(r).exportEnergy() * Math.pow(lossConstant, nodeSpacing * Math.abs(r - i)));
					}
				}
				else {
					break;
				}
			}
			
			if (n.hasEnergyDeficit()) {
				
				
				if (i > 0) {
					n.importEnergy(nodeList.get(i - 1).exportEnergy() * Math.pow(lossConstant, nodeSpacing));
					if (n.hasEnergyDeficit()) {
						if (i < nodeList.size() - 1) {
							n.importEnergy(nodeList.get(i + 1).exportEnergy() * Math.pow(lossConstant, nodeSpacing));
						}
						else if (i - 1 > 0) {
							n.importEnergy(nodeList.get(i - 2).exportEnergy() * Math.pow(lossConstant, 2 * nodeSpacing));
						}
					}
				}
				else if (i < nodeList.size() - 1) {
					n.importEnergy(nodeList.get(i + 1).exportEnergy() * Math.pow(lossConstant, nodeSpacing));
					if (n.hasEnergyDeficit()) {
						if (i + 1 < nodeList.size() - 1) {
							n.importEnergy(nodeList.get(i + 2).exportEnergy() * Math.pow(lossConstant, 2 * nodeSpacing));
						}
					}
				}
			}*/
	}
	
	public double getNodeSpacing() {
		return nodeSpacing;
	}

	public void setNodeSpacing(double nodeSpacing) {
		this.nodeSpacing = nodeSpacing;
	}	

	public double getEnergyTotalGenerated() {
		return energyTotalGenerated;
	}

	public void setEnergyTotalGenerated(double energyTotalGenerated) {
		this.energyTotalGenerated = energyTotalGenerated;
	}

	public double getEnergyTotalRequired() {
		return energyTotalRequired;
	}

	public void setEnergyTotalRequired(double energyTotalRequired) {
		this.energyTotalRequired = energyTotalRequired;
	}

	public double getLossConstant() {
		return lossConstant;
	}

	public void setLossConstant(double lossConstant) {
		this.lossConstant = lossConstant;
	}

	public double getGenerationCap() {
		return generationCap;
	}

	public void setGenerationCap(double generationCap) {
		this.generationCap = generationCap;
	}

	public double getRequirementCap() {
		return requirementCap;
	}

	public void setRequirementCap(double requirementCap) {
		this.requirementCap = requirementCap;
	}
	
	public double getGenerationFloor() {
		return generationFloor;
	}

	public void setGenerationFloor(double generationFloor) {
		this.generationFloor = generationFloor;
	}

	public static void main(String[] args) {
		int m = 2;
		int n = 2;
		double gmax = 10;
		double dmax = 10;
		Network net = new Network(m, n, 1, 0.9, gmax, dmax);
		Node[][] ndList = net.getNodeArray();
		
		int numberOfBands = 20;
		double bandWidth = gmax / (numberOfBands);
		double[][] energyBands = new double[numberOfBands][3];
	
		for (int i=0; i < numberOfBands; i++) {
			energyBands[i][0] = i * bandWidth;
		}
		
		int failCount = 0;
		int failCount1 = 0;
		boolean fail;
		int runNumber = 1000;
		double aveGeneration;
		double aveDemand;
		double sumGeneration;
		double sumDemand;
		
		for (int i=0; i < runNumber; i++) {
			fail = false;
			aveGeneration = 0;
			aveDemand = 0;
			sumGeneration = 0;
			sumDemand = 0;
			
			System.out.println("Run " + i);
			
			for (int y = net.getVerticalNumberOfNodes() - 1; y >= 0; y--) {
				for (int x=0; x < net.getHorizontalNumberOfNodes(); x++) {
					System.out.printf("%.7f  ", ndList[x][y].getEnergyNet());
					if (ndList[x][y].getEnergyNet() < 0) {
						fail = true;
					}
					sumGeneration += ndList[x][y].getEnergyGenerated();
					sumDemand += ndList[x][y].getEnergyRequired();
					System.out.println("energy generation: " + ndList[x][y].getEnergyGenerated());
					System.out.println("energy demand: " + ndList[x][y].getEnergyRequired());
				}
				System.out.println();
			}
			
			if (fail) {
				++failCount;
				fail = false;
			}
			aveGeneration = sumGeneration / (net.getVerticalNumberOfNodes() * net.getHorizontalNumberOfNodes());
			aveDemand = sumDemand / (net.getVerticalNumberOfNodes() * net.getHorizontalNumberOfNodes());
			
			boolean eject1 = false;
			boolean eject2 = false;
			for (int j=0; j < numberOfBands; j++) {
				if (j == numberOfBands - 1) {
					if (aveGeneration > energyBands[j][0]) {
						energyBands[j][1]++;
					}
					if (aveDemand > energyBands[j][0]) {
						energyBands[j][2]++;
					}
				} else {
					if (aveGeneration > energyBands[j][0] && aveGeneration < energyBands[j+1][0]) {
						energyBands[j][1]++;
						eject1 = true;
					}
					if (aveDemand > energyBands[j][0] && aveDemand < energyBands[j+1][0]) {
						energyBands[j][2]++;
						eject2 = true;
					}
					if (eject1 && eject2) break;
				}
			}
			
			System.out.println("Average generation: " + aveGeneration + "  Average demand: " + aveDemand);
			
			net.redistributeEnergy();
			
			for (int y = net.getVerticalNumberOfNodes() - 1; y >= 0; y--) {
				for (int x=0; x < net.getHorizontalNumberOfNodes(); x++) {
					System.out.printf("%.7f  ", ndList[x][y].getEnergyNet());
					if (ndList[x][y].getEnergyNet() < 0) {
						fail = true;
					}
				}
				System.out.println();
			}
			
			if (fail) {
				++failCount1;
			}
			
			net.generateNodes();
		}
		
		for (int i=0; i < numberOfBands; i++) {
			System.out.println("Band: " + energyBands[i][0] + " Count1: " + energyBands[i][1] + " Count2: " + energyBands[i][2]);
		}
		
		System.out.println("Fail count before redistribution = " + failCount + "/" + runNumber);
		System.out.println("Fail count after redistribution = " + failCount1 + "/" + runNumber);
		
		
	}

	
	
}
