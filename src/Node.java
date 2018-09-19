
public class Node {
	
	private double energyNet;
	private double energyGenerated;
	private double energyRequired;
	private double energyReceived;
	private double energyExported;
	private boolean energySurplus;
	private boolean energyDeficit;
	
	public Node() {
		energyNet = 0;
		energyGenerated = 0;
		energyRequired = 0;
		energyReceived = 0;
		energyExported = 0;
		energySurplus = false;
		energyDeficit = false;
	}
	
	public void resetNode() {
		energyNet = 0;
		energyGenerated = 0;
		energyRequired = 0;
		energyReceived = 0;
		energyExported = 0;
		energySurplus = false;
		energyDeficit = false;
	}
	
	public double getEnergyGenerated() {
		return energyGenerated;
	}
	
	public void setEnergyGenerated(double energyGenerated) {
		this.energyGenerated = energyGenerated;
		calculateNetEnergy();
	}
	
	public double getEnergyRequired() {
		return energyRequired;
	}
	
	public void setEnergyRequired(double energyRequired) {
		this.energyRequired = energyRequired;
		calculateNetEnergy();
	}
	
	public double getEnergyReceived() {
		return energyReceived;
	}
	
	public void importEnergy(double energyAmount) {
		this.energyReceived += energyAmount;
		calculateNetEnergy();
	}
	
	public double getEnergyExported() {
		return energyExported;
	}
	
	public double exportEnergy() {
		if (energySurplus) {
			Double temp = energyExported;
			energyExported += energyNet;
			calculateNetEnergy();
			return energyExported - temp;
		}
		else {
			return 0;
		}
	}

	public double getEnergyNet() {
		return energyNet;
	}

	private void calculateNetEnergy() {
		energyNet = energyGenerated - energyRequired + energyReceived - energyExported;
		
		if (energyNet > 0) {
			energySurplus = true;
			energyDeficit = false;
		}
		else if (energyNet == 0) {
			energySurplus = false;
			energyDeficit = false;
		}
		else {
			energySurplus = false;
			energyDeficit = true;
		}
	}

	public boolean hasEnergySurplus() {
		return energySurplus;
	}

	public boolean hasEnergyDeficit() {
		return energyDeficit;
	}
	
}
