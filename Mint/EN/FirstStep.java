import java.util.HashSet;
import java.util.Set;


public class FirstStep {
	public int numDenominations;
	private void assign(int[] arr, int ind, int value, Set<Integer> changedNumbers) {
		arr[ind] = value;
		changedNumbers.add(ind);
		arr[100 - ind] = value;
		changedNumbers.add(100 - ind);
	}
	
	private int[] exchangeNumber(int[] arr) {
		int[] sums = new int[2];
		for (int n = 1; n < 100; n++) {
			if (n % 5 == 0)
				sums[1] += arr[n];
			else
				sums[0] += arr[n];
		}
		return sums;
	}
	
	private void calc(int[] denominations, int right, int[] exchangeNumberArr) {
		int n = denominations[denominations.length - 1];
		Set<Integer> changedNumbers = new HashSet<Integer>();
		if (right != 100)
			assign(exchangeNumberArr, right, 1, changedNumbers);
		for (int num = n + 1; num < right; num++) {
			for (int i = 1; i < num / 2 + 1; i++) {
				int temp = exchangeNumberArr[i] + exchangeNumberArr[num - i];
				if (temp < exchangeNumberArr[num])
					assign(exchangeNumberArr, num, temp, changedNumbers);
			}
			if (exchangeNumberArr[right - num] + 1 < exchangeNumberArr[num])
				assign(exchangeNumberArr, num, exchangeNumberArr[right - num] + 1, changedNumbers);
		}
		if (right == 100)
			changedNumbers.add(0);
		while (changedNumbers.size() != 0) {
			Set<Integer> newChangedNumbers = new HashSet<Integer>();
			newChangedNumbers.addAll(changedNumbers);
			changedNumbers = new HashSet<Integer>();
			for (int num = 1; num < right; num++) {
				for (int i : newChangedNumbers) {
					if (i < num) {
						int temp = exchangeNumberArr[i] + exchangeNumberArr[num - i];
						if (temp < exchangeNumberArr[num])
							assign(exchangeNumberArr, num, temp, changedNumbers);
					}
					else if (i > num) {
						int temp = exchangeNumberArr[i] + exchangeNumberArr[i - num];
						if (temp < exchangeNumberArr[num])
							assign(exchangeNumberArr, num, temp, changedNumbers);
					}
					if (i + num < right + 1) {
						int temp = exchangeNumberArr[i] + exchangeNumberArr[i + num];
						if (temp < exchangeNumberArr[num])
							assign(exchangeNumberArr, num, temp, changedNumbers);
					}
				}
			}
		}
	}
	
	private void preCalc(int[] denominations, int[] exchangeNumberArr) {
		if (denominations.length == numDenominations) {
			int[] newArr = new int[exchangeNumberArr.length];
			for (int i = 0; i < exchangeNumberArr.length; i++)
				newArr[i] = exchangeNumberArr[i];
			calc(denominations, 100, newArr);
			int[] sums = exchangeNumber(newArr);
			System.out.print("[");
			System.out.print(denominations[0]);
			for (int i = 1; i < denominations.length; i++) {
				System.out.print(", " + denominations[i]);
			}
			System.out.print("] ");
			System.out.println(sums[0] + " " + sums[1]);
		}
		else {
			if (denominations.length == 0) {
				for (int i = 1; i < 96; i++) {
					int[] newArr = new int[exchangeNumberArr.length];
					for (int j = 0; j < exchangeNumberArr.length; j++)
						newArr[j] = exchangeNumberArr[j];
					assign(newArr, i, 1, new HashSet<Integer>());
					int[] newDenominations = new int[1];
					newDenominations[0] = i;
					preCalc(newDenominations, newArr);
				}
			}
			else {
				for (int i = denominations[denominations.length - 1] + 1; i < 96 + denominations.length; i++) {
					int[] newArr = new int[exchangeNumberArr.length];
					for (int j = 0; j < exchangeNumberArr.length; j++)
						newArr[j] = exchangeNumberArr[j];
					calc(denominations, i, newArr);
					int[] newDenominations = new int[denominations.length + 1];
					for(int j = 0; j < denominations.length; j++)
						newDenominations[j] = denominations[j];
					newDenominations[denominations.length] = i;
					preCalc(newDenominations, newArr);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		int[] arr = new int[0];
		int[] exchangeNumber = new int[101];
		for (int i = 1; i < 100; i++) 
			exchangeNumber[i] = 100;
		FirstStep firstStep = new FirstStep();
		firstStep.numDenominations = 5;
		firstStep.preCalc(arr, exchangeNumber);
	}
}
