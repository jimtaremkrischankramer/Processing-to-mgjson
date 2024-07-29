package tomgjson;


import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.lang.Math;
import processing.data.JSONObject;


public class ToMgjson {

	public static String mgjson = "";
	public static String dataOutlines = "";
	
	public static String header = "\n"
			+ "  \"version\": \"MGJSON2.0.0\",\n"
			+ "  \"creator\": \"SILKE | JIM\",\n"
			+ "  \"dynamicSamplesPresentB\": true,\n"
			+ "  \"dynamicDataInfo\": {\n"
			+ "    \"useTimecodeB\": false,\n"
			+ "    \"utcInfo\": {\n"
			+ "      \"precisionLength\": 3,\n"
			+ "      \"isGMT\": true\n"
			+ "    }\n"
			+ "  },";

	public static int sampleCount;
	public static int frameTime = 40;
	public static int frameCount;
	public static int digitsInteger = 5;
	public static int digitsDecimal = 2;
	public static boolean isSigned = true;
	public static String filePath;

	public static Object [] streams;
	public static String[] streamNames;

	public static String[] dataDynamicSamples;
	public static JSONObject rangeJSON;
	

	public ToMgjson(int count, String path, String[] names, Integer... d) {
		digitsInteger = d.length > 0 ? d[0] : 5;
		digitsDecimal = d.length > 1 ? d[1] : 2;
	    
		filePath = path;
		sampleCount = count;
		streams = new Object[names.length];

		  rangeJSON = new JSONObject();

		  streamNames = names;

		  dataDynamicSamples = new String[streamNames.length];

		  for (int i = 0; i < streamNames.length; ++i) {
		    JSONObject minMaxJSON;
		    minMaxJSON = new JSONObject();
		    minMaxJSON.setInt("min", 0);
		    minMaxJSON.setInt("max", 0);
		    rangeJSON.setJSONObject(streamNames[i], minMaxJSON);

		    dataDynamicSamples[i] = "";
	    }
	}
	
	public void updateStreams(int frame, Object[] streamValues) {
		frameCount = frame;
		if(streamValues.length == streamNames.length){
		    for (int i = 0; i < streamValues.length; ++i) {
		    	if(streamValues[i] instanceof Integer) {
		    		streams[i] = (Integer) streamValues[i];
		    	} else if (streamValues[i] instanceof Float) {
		    		streams[i] = (Float) streamValues[i];
		    	} else if (streamValues[i] instanceof Double) {
		    		streams[i] = (Double) streamValues[i];
		    	}
		    }
		    
		    for (int i = 0; i < streamNames.length; ++i) {
		    	int [] minMax = {
		    	rangeJSON.getJSONObject(streamNames[i]).getInt("min"),
		    	rangeJSON.getJSONObject(streamNames[i]).getInt("max")};
		      
		    	JSONObject minMaxJSON;
		    	minMaxJSON = new JSONObject();
		    	minMaxJSON.setInt("min", minMax((Number) streams[i], minMax)[0]);
		    	minMaxJSON.setInt("max", minMax((Number) streams[i], minMax)[1]);
		    	rangeJSON.setJSONObject(streamNames[i], minMaxJSON);
	
		    	// DATA DYNAMIC SAMPLES
		    	Object streamValue = streams[i];
		    	
		    	if(streamValue instanceof Integer) {
		    		int value = (int) streamValue;
		    		dataDynamicSamples[i] = dataDynamicSamples[i] + sampleString(frameCount - 1, value);
		    	} else if(streamValue instanceof Float) {
		    		float value = (float) streamValue;
		    		dataDynamicSamples[i] = dataDynamicSamples[i] + sampleString(frameCount - 1, value);
		    	} else if (streamValue instanceof Double){
		    		double value = (double) streamValue;
		    		dataDynamicSamples[i] = dataDynamicSamples[i] + sampleString(frameCount - 1, value);
				} else {
					throw new IllegalArgumentException("number must be int or float");
				}
		    }
		} else {
			throw new IllegalArgumentException("Array must have same length as displayNames");
		}
		
		write(filePath);
	}
	
	private void write(String path){
		if(frameCount == sampleCount){
			for (int i = 0; i < streamNames.length; ++i) {
				// DATA OUTLINE
				int [] minMax = {
				rangeJSON.getJSONObject(streamNames[i]).getInt("min"),
				rangeJSON.getJSONObject(streamNames[i]).getInt("max")};
				dataOutlines += dataOutline("dataDynamic", streamNames[i], i, "numberString", digitsInteger, digitsDecimal, isSigned, minMax[0], minMax[1], sampleCount);
				if (i == streamNames.length - 1) {
					  dataOutlines = dataOutlines.substring( 0, dataOutlines.length() - 2); 
				}
			}
			
			mgjson = assemble(header, dataOutlines, dataDynamicSamples(dataDynamicSamples));
			
			try {
	            // Attempt to create and write to the file
	            PrintWriter output = new PrintWriter(path);
	            output.print(mgjson);
	            output.close();
	            System.out.println("File created successfully.");
	        } catch (FileNotFoundException e) {
	            System.err.println("FileNotFoundException: Could not create or find the file.");
	            e.printStackTrace();
	        } catch (Exception e) {
	            System.err.println("Exception: An unexpected error occurred.");
	            e.printStackTrace();
	        }
		
		} 
		  
	}
	
	private String digits(int x){
	    return "%0" + x + "d";
	}
	
	private <T extends Number> String sampleString(int frame, T value) {
	    String re = String.format(
	        "\n          {\n" +
	        "            \"time\": \"1970-01-01T00:00:%02d.%03dZ\",\n" +
	        "            \"value\": \"%s\"\n" +
	        "          },",
	        (int) Math.floor((frame * frameTime) / 1000),
	        Integer.parseInt(lastDigits(String.format("%03d", frame * frameTime))),
	        value(value)
	    );

	    if (frame == sampleCount - 1) {
	        re = re.substring(0, re.length() - 1); // Remove the trailing comma for the last frame
	    }
	    
	    return re;
	}
	
	private String lastDigits(String num){
	    if (num.length() == 3) {
	        return num;
	    } else if (num.length() > 3) {
	        return num.substring(num.length() - 3);
	    } else {
	        throw new IllegalArgumentException("num has fewer than 3 digits!");
	    }
	}
	
	private String getDecimalPart(double value, int digitsDecimal) {
        double fractionalPart = value - Math.floor(value);
        String formatted = String.format("%." + digitsDecimal + "f", fractionalPart);
        // Removes the "0." prefix
        return formatted.substring(2);
    }
	
	private <T extends Number> String value(T value){
		String decimals;
		int ints;
		if (value instanceof Float) {
			double v = value.doubleValue();
			ints = (int) Math.floor(v);
			decimals = getDecimalPart(v, digitsDecimal);
		} else if(value instanceof Double) { 
			ints = (int) Math.floor((double) value.doubleValue());
			decimals = getDecimalPart((double)value, digitsDecimal);
		} else if(value instanceof Integer) {
			ints = (int) Math.floor((double) value.doubleValue());
			decimals = new String(new char[digitsDecimal]).replace("\0", "0");
		} else {
			throw new IllegalArgumentException("number must be int or float");
		}
		
	    if (ints >= 0) {    
	        return "+" + String.format(
	            digits(digitsInteger), ints) +
	            "." + decimals;
	    } else {
	        return String.format(
	            digits(digitsInteger + 1), ints) +
	            "." + decimals;
	    } 
	}
	
	private <T extends Number> int[] minMax(T value, int[] curr) {
		int val;
		if (value instanceof Float) {
			val = (int) Math.ceil(value.doubleValue());
		} else if(value instanceof Double) {
			val = (int) Math.ceil(value.doubleValue());
		} else if(value instanceof Integer) {
			val = value.intValue();
		} else {
			throw new IllegalArgumentException("minMax value must be int or float");
		}
			int [] re = new int [2];
			int min = curr[0];
			int max = curr[1];
			if (val < min) {
				re[0] = val;
				re[1] = curr[1];
			} else if (val > max) {
				re[0] = curr[0];
				re[1] = val;
			} else {
				re = curr;
			}
			return re;
		}
	
	private String dataOutline(
		    String objectType,
		    String displayName,
		    int streamIndex,
		    String type,
		    int digInt,
		    int digDec,
		    boolean isSigned,
		    int min,
		    int max,
		    int samples
		) {
		    String re = String.format(
		        "{\n" +
		        "      \"objectType\": \"%s\",\n" +
		        "      \"displayName\": \"%s\",\n" +
		        "      \"sampleSetID\": \"Stream%d\",\n" +
		        "      \"dataType\": {\n" +
		        "        \"type\": \"%s\",\n" +
		        "        \"numberStringProperties\": {\n" +
		        "          \"pattern\": {\n" +
		        "            \"digitsInteger\": %d,\n" +
		        "            \"digitsDecimal\": %d,\n" +
		        "            \"isSigned\": %s\n" +
		        "          },\n" +
		        "          \"range\": {\n" +
		        "            \"occuring\": {\n" +
		        "              \"min\": %d,\n" +
		        "              \"max\": %d\n" +
		        "            },\n" +
		        "            \"legal\": {\n" +
		        "              \"min\": %d,\n" +
		        "              \"max\": %d\n" +
		        "            }\n" +
		        "          }\n" +
		        "        },\n" +
		        "        \"paddedStringProperties\": {\n" +
		        "          \"maxLen\": 0,\n" +
		        "          \"maxDigitsInStrLength\": 0,\n" +
		        "          \"eventMarkerB\": false\n" +
		        "        }\n" +
		        "      },\n" +
		        "      \"interpolation\": \"linear\",\n" +
		        "      \"hasExpectedFrequencyB\": false,\n" +
		        "      \"sampleCount\": %d,\n" +
		        "      \"matchName\": \"Stream%d\"\n" +
		        "    },\n",
		        objectType, displayName, streamIndex, type, digInt, digDec, isSigned, min, max, min, max, samples, streamIndex
		    );

	    return re;
	}
	
	private String assemble(String head, String outlines, String samples) {
	    String re = String.format(
	        "{\n" +
	        "    %s\n" +
	        "    \"dataOutline\": [%s  ],\n" +
	        "    \"dataDynamicSamples\": [\n" +
	        "      %s\n" +
	        "    ]\n" +
	        "  }", head, outlines, samples);
	    return re;
	}
	
	private String dataDynamicSamples(String[] dataDynamicSamples) {
	    StringBuilder re = new StringBuilder();
	    for (int i = 0; i < dataDynamicSamples.length; ++i) {
	        re.append(String.format(
	            "\n     {\n" +
	            "       \"sampleSetID\": \"Stream%d\",\n" +
	            "         \"samples\": [%s]\n" +
	            "     },\n" +
	            "     ", i, dataDynamicSamples[i]));
	    }
	    // Remove the last extra comma and newline
	    int lastIndex = re.lastIndexOf(",\n     ");
	    if (lastIndex != -1) {
	        re.delete(lastIndex, lastIndex + 6);
	    }
	    return re.toString();
	}
}

