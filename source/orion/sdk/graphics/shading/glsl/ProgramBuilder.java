package orion.sdk.graphics.shading.glsl;

import orion.sdk.util.Transform;

public abstract class ProgramBuilder
{
	protected static final String NL = "\n";
	protected static final String TB0 = "   ";
	protected static final String TB1 = TB0 + TB0;
	protected static final String TB2 = TB1 + TB0;
	protected static final String TB3 = TB2 + TB0;
	protected static final String TB4 = TB3 + TB0;
	protected static final String TB5 = TB4 + TB0;
	protected static final String TB6 = TB5 + TB0;
	protected static final String TB7 = TB6 + TB0;
	protected static final String TB8 = TB7 + TB0;
	protected static final String TB9 = TB8 + TB0;
	
	protected static String[] addLineNumbers(String[] source, int lineNumberLength)
	{
		String[] output = new String[source.length];
		for (int i = 0; i < source.length; i++)
		{
			String lineNumber = Transform.pad(Integer.toString(i + 1), lineNumberLength, ' ', true);
			output[i] = lineNumber + source[i]; 
		}
		return output;
	}
}
