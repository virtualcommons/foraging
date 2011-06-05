package edu.asu.commons.foraging.jcal3d.misc;

public class Error {
	public static final int OK = 0;

	public static final int INTERNAL = 1;

	public static final int INVALID_HANDLE = 2;

	public static final int MEMORY_ALLOCATION_FAILED = 3;

	public static final int FILE_NOT_FOUND = 4;

	public static final int INVALID_FILE_FORMAT = 5;

	public static final int FILE_PARSER_FAILED = 6;

	public static final int INDEX_BUILD_FAILED = 7;

	public static final int NO_PARSER_DOCUMENT = 8;

	public static final int INVALID_ANIMATION_DURATION = 9;

	public static final int BONE_NOT_FOUND = 10;

	public static final int INVALID_ATTRIBUTE_VALUE = 11;

	public static final int INVALID_KEYFRAME_COUNT = 12;

	public static final int INVALID_ANIMATION_TYPE = 13;

	public static final int FILE_CREATION_FAILED = 14;

	public static final int FILE_WRITING_FAILED = 15;

	public static final int INCOMPATIBLE_FILE_VERSION = 16;

	public static final int NO_MESH_IN_MODEL = 17;

	public static final int BAD_DATA_SOURCE = 18;

	public static final int NULL_BUFFER = 19;

	public static final int INVALID_MIXER_TYPE = 20;

	public static final int MAX_ERROR_CODE = 21;

	protected static int lastErrorCode = OK;

	protected static String lastErrorFile = new String();

	protected static int lastErrorLine = -1;

	protected static String lastErrorText = new String();

	/*
	 * This function returns the code of the last error that occured inside the
	 * library.
	 */
	public static int getLastErrorCode() {
		return lastErrorCode;
	}

	/*
	 * This function returns the name of the file where the last error occured.
	 */
	public static String getLastErrorFile() {
		return lastErrorFile;
	}

	/*
	 * This function returns the line number where the last error occured.
	 */
	public static int getLastErrorLine() {
		return lastErrorLine;
	}

	/*
	 * This function returns the suppementary text of the last error occured
	 * inside the library.
	 */
	public static String getLastErrorText() {
		return lastErrorText;
	}

	/*
	 * This function dumps all the information about the last error that occured
	 * inside the library to the standard error device.
	 */
	public static void printLastError() {
		System.err.print("cal3d : " + getLastErrorDescription());

		// only print supplementary information if there is some
		if (lastErrorText.length() > 0)
			System.err.print(" '" + lastErrorText + "'");

		if (lastErrorFile.length() > 0)
			System.err.print(" in " + lastErrorFile);

		if (lastErrorLine != -1)
			System.err.print("(" + lastErrorLine + ")");

		System.err.println();
	}

	/*
	 * This function sets all the information about the last error that occured
	 * inside the library.
	 * 
	 * @param code The code of the last error. @param strFile The file where the
	 * last error occured. @param line The line number where the last error
	 * occured. @param strText The supplementary text of the last error.
	 */
	public static void setLastError(int code, String strFile, int line,
			String strText) {
		if (code >= MAX_ERROR_CODE)
			code = INTERNAL;

		lastErrorCode = code;
		lastErrorFile = strFile;
		lastErrorLine = line;
		lastErrorText = strText;
	}

	/*
	 * This function returns a short description of the last error that occured
	 * inside the library.
	 */
	public static String getErrorDescription(int code) {
		switch (code) {
		case OK:
			return "No error found";
		case INTERNAL:
			return "Internal error";
		case INVALID_HANDLE:
			return "Invalid handle as argument";
		case MEMORY_ALLOCATION_FAILED:
			return "Memory allocation failed";
		case FILE_NOT_FOUND:
			return "File not found";
		case INVALID_FILE_FORMAT:
			return "Invalid file format";
		case FILE_PARSER_FAILED:
			return "Parser failed to process file";
		case INDEX_BUILD_FAILED:
			return "Building of the index failed";
		case NO_PARSER_DOCUMENT:
			return "There is no document to parse";
		case INVALID_ANIMATION_DURATION:
			return "The duration of the animation is invalid";
		case BONE_NOT_FOUND:
			return "Bone not found";
		case INVALID_ATTRIBUTE_VALUE:
			return "Invalid attribute value";
		case INVALID_KEYFRAME_COUNT:
			return "Invalid number of keyframes";
		case INVALID_ANIMATION_TYPE:
			return "Invalid animation type";
		case FILE_CREATION_FAILED:
			return "Failed to create file";
		case FILE_WRITING_FAILED:
			return "Failed to write to file";
		case INCOMPATIBLE_FILE_VERSION:
			return "Incompatible file version";
		case NO_MESH_IN_MODEL:
			return "No mesh attached to the model";
		case BAD_DATA_SOURCE:
			return "Cannot read from data source";
		case NULL_BUFFER:
			return "Memory buffer is null";
		case INVALID_MIXER_TYPE:
			return "The CalModel mixer is not a CalMixer instance";
		default:
			return "Unknown error";
		}
	}

	public static String getLastErrorDescription() {
		return getErrorDescription(getLastErrorCode());
	}
}
