package edu.asu.commons.foraging.data;

import java.awt.Dimension;
import java.util.Arrays;

/**
 *
 * Utility class to keep track of spatial distribution statistics.
 * 
 * 
 */
class ClientSpatialDistribution {
    int[] rowCounts;
    private double standardizedRowDistribution;
    int[] columnCounts;
    private double standardizedColumnDistribution;
    int tokens = 0;
    double rowStandardDeviation;
    double columnStandardDeviation;
    double weightedSpatialMetric;

    ClientSpatialDistribution(Dimension boardSize) {
        rowCounts = new int[boardSize.height];
        columnCounts = new int[boardSize.width];
        zeroRowColumnCounts();
    }
    
    public void zeroRowColumnCounts() {
        Arrays.fill(rowCounts, 0);
        Arrays.fill(columnCounts, 0);
    }
    
    public String toString() {
        return String.format("tokens: %d, row: %s, col: %s", tokens, standardizedRowDistribution, standardizedColumnDistribution);
    }
    
    void calculateStandardDeviation() {
        rowStandardDeviation = stdDev(rowCounts);
        columnStandardDeviation = stdDev(columnCounts);
        double averageTokens = (double) tokens / (double) rowCounts.length;
        standardizedRowDistribution = (rowStandardDeviation / averageTokens);
        standardizedColumnDistribution = (columnStandardDeviation / averageTokens);
        weightedSpatialMetric = tokens * (rowStandardDeviation + columnStandardDeviation);
    }

    private double stdDev(int[] counts) {
        // calculate mean
        int totalTokensInRow = sum(counts);
        double size = (double) counts.length;
        double mean = (double) totalTokensInRow / size;
        double sumOfSquares = 0;
        for (int count : counts) {
            double difference = count - mean;
            sumOfSquares += (difference * difference); 
        }
        return Math.sqrt(sumOfSquares / size);
    }
    private int sum(int[] count) {
        int total = 0;
        for (int tokens: count) {
            total += tokens;
        }
        return total;
    }
}