package orion.sdk.monitoring.incidents;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.imageio.ImageIO;

import orion.sdk.graphics.shading.glsl.ProgramBuilder;
import orion.sdk.graphics.util.OpenGLManager;
import orion.sdk.util.Shell;

public class IncidentTrace extends ProgramBuilder implements IIncidentListener
{
	private static final int PAGE_SIZE = 10000;
	private static final int PADDING = 5;
	private static final int BORDER_RADIUS = PADDING * 2;
	private static final int INDENT = PADDING;
	
	protected PrintWriter writer = null;
	protected int page = 1;
	protected int incidentId = 0;
	protected int attachmentId = 0;
	protected String name = null;
	protected String directory = null;
	
	public IncidentTrace(String directory, String name)
	{
		this.name = name;
		this.directory = directory;
	}
	
	private void delete(File file)
	{		
		if (file.isDirectory())
		{
			File[] contents = file.listFiles();
			for (File child : contents)
			{
				delete(child);
			}
		}
		else
		{
			file.delete();
		}
	}
	
	public synchronized void open() throws Exception
	{
		/*
		 * Clean tracing directory
		 */
		File directory = new File(this.directory);
		if (directory.exists())
		{
			delete(directory);
		}
		
		Shell.makeDirectories(directory);
		Shell.makeDirectories(new File(directory.getAbsolutePath() + File.separator + "Resources"));
		
		/*
		 * Perform first cutover
		 */
		this.cutoverTrace();		
	}

	public void close() throws Exception
	{				
		if (writer != null)
		{
			writer.println(TB0 + "</div>");
			this.writer.println("</BODY>");
			this.writer.println("</HTML>");
			writer.flush();
			writer.close();
		}
	}
	
	public String getHex(Color color)
	{
		int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
		
		return String.format("%02x%02x%02x", r, g, b);
	}
	
	
	protected void cutoverTrace() throws Exception
	{
		this.close();
		
		String filename = this.directory + File.separator + this.name + "_" + page + ".html";
		
		this.writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(filename))));
		this.page++;
		
		this.writer.println("<HTML>");
		this.writer.println("<HEAD>");
		this.writer.println(this.getStyle());
		this.writer.println(this.getScript());
		this.writer.println("</HEAD>");
		this.writer.println("<BODY>");
		this.printHeader();
		this.incidentId = 0;
	}
	
	protected void printHeader()
	{
		writer.println(TB0 + "<div style='width:2000px'>");
		
		writer.println(TB0 + "<table>");
		writer.println(TB0 + "<tr style='padding: 0px;'>");
		
		writer.println(TB0 + "<td style='padding: 0px; width:120px;'>");
		writer.println(TB1 + "<div style='text-align:center; font-weight:bold; color:#d3d3d3; margin-left:" + INDENT + "px; border: 1px solid #d3d3d3; background-color:#" + getHex(Color.WHITE) + "; border-radius: " + BORDER_RADIUS + "px;padding: " + PADDING + "px;cursor:pointer;' onclick='toggleByClass(\"child\");'>");
		writer.println(TB2 + "<code>");
		writer.println(TB2 + "<span>");
		writer.println(TB2 + "Toggle all");
		writer.println(TB2 + "</span>");
		writer.println(TB2 + "</code>");
		writer.println(TB1 + "</div>");
		writer.println(TB0 + "</td>");
		
		writer.println(TB0 + "</tr>");
		writer.println(TB0 + "</table>");
	}
	
	protected Color applyAlpha(Color color, float alpha)
	{
		int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
		
		r = Integer.max(0, Integer.min((int) (r + alpha * 255), 255));
		g = Integer.max(0, Integer.min((int) (g + alpha * 255), 255));
		b = Integer.max(0, Integer.min((int) (b + alpha * 255), 255));
		
		return new Color(r, g, b);
	}
	
	protected Color getIncidentColor(Incident incident)
	{
		switch (incident.getType())
		{
			case Incident.Type.INFORMATION:
				return Color.BLUE;
			case Incident.Type.WARNING:
				return Color.ORANGE;
			case Incident.Type.DEBUG:
				return Color.MAGENTA;
			case Incident.Type.OPENGL:
				return applyAlpha(Color.GREEN, -0.5f);
		}
		
		if (containsError(incident))
		{
			return Color.RED;
		}
		
		return Color.BLACK;
	}
	
	protected static String htmlSafeString(String string)
	{
		string = string.replaceAll("<", "&#60;");
		string = string.replaceAll(">", "&#62;");
		string = string.replaceAll(" ", "&nbsp;");
		
		string = string.replaceAll("&#60;hr&#62;", "<hr>");
		
		return string;
	}
	
	protected String getStyle()
	{
		return
			TB0 + "<style>" + NL +
			
				TB1 + "p {" + NL +
					TB2 + "line-height: 1;" + NL +
					TB2 + "margin-top: 2;" + NL +
					TB2 + "margin-bottom: 2;" + NL +
				TB1 + "}" + NL +
				
				TB1 + "pre, code {" + NL +
					TB2 + "margin-top: 0;" + NL +
					TB2 + "margin-bottom: 0;" + NL +
				TB1 + "}" + NL +
				
			TB0 + "</style>";
				
	}
	
	protected String getScript()
	{
		return
			TB0 + "<script type='text/javascript'>" + NL +
			
				TB1 + "function toggleById(id)" + NL +
				TB1 + "{" + NL +
					TB2 + "var e = document.getElementById(id);" + NL +
					TB2 + "if (e.style.display == 'block')" + NL +
						TB3 + "e.style.display = 'none';" + NL +
					TB2 + "else" + NL +
						TB3 + "e.style.display = 'block';" + NL +
				TB1 + "}" + NL +
				
				TB1 + "function toggleByClass(className)" + NL +
				TB1 + "{" + NL +
					TB2 + "var x = document.getElementsByClassName(className);" + NL +
					TB2 + "for (i = 0; i < x.length; i++)" + NL +
					TB2 + "{"  + NL +
						TB3 +  "var e = x[i];" + NL +
						TB3 + "if (e.style.display == 'block')" + NL +
							TB4 + "e.style.display = 'none';" + NL +
						TB3 + "else" + NL +
							TB4 + "e.style.display = 'block';" + NL +
					TB2 + "}"  + NL +
				TB1 + "}" + NL +
				
			TB0 + "</script>";
	}	
	
	protected static String formatOpenGL(String line)
	{
		String current = line;
		
		Collection<String> glConstants = OpenGLManager.getInstance().getGLConstants().values();
		for (String constant : glConstants)
		{
			while (current.contains("." + constant + "."))
			{
				String formattedConstant = "<i><span style='color:grey'>[" + constant + "]</span></i>";
				current = current.replaceAll("." + constant + ".", formattedConstant);
			}
		}

		List<String> glFunctions = OpenGLManager.getInstance().getGLFunctions();
		for (String function : glFunctions)
		{
			while (current.contains(function + "("))
			{
				String formattedFunction = "<b><span style='color:black'>" + function + "</span></b>";
				current = current.replaceAll(function, formattedFunction);
			}
		}
		
		Map<String, String> toReplace = new HashMap<String, String>();
		toReplace.put("&#60;int&#62;", "int");
		toReplace.put("&#60;double&#62;", "double");
		toReplace.put("&#60;float&#62;", "float");
		toReplace.put("&#60;java.lang.String&#62;", "string");
		
		for (String nextToReplace : toReplace.keySet())
		{
			while (current.contains(nextToReplace))
			{
				current = current.replaceAll(nextToReplace, "<span style='color:grey'>" + toReplace.get(nextToReplace) + "</span>");
			}
		}

		return current;
	}
	
	protected boolean containsError(Incident incident)
	{
		if (incident.getType() == Incident.Type.ERROR)
		{
			return true;
		}
		
		List<Incident> children = incident.getChildren();
		for (Incident child : children)
		{
			if (containsError(child))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public synchronized void processIncidents(List<Incident> incidents) throws Exception
	{
		for (Incident incident : incidents)
		{
			this.processIncident(incident);
		}
	}
	
	public synchronized void processIncident(Incident incident) throws Exception
	{
		if (!incident.hasContent())
		{
			return;
		}
		
		this.incidentId += 2;
		
		Object[] attachments = incident.getAttachments();
		String message = formatOpenGL(htmlSafeString(incident.getMessage()));				
		boolean hasAttachments = attachments.length > 0;
		boolean hasChildren = incident.getChildren().size() > 0;
		String type = Incident.Type.toString(incident.getType());		
		
		Color incidentColor = getIncidentColor(incident);
		Color backgroundColor = incident.getType() == Incident.Type.DEBUG ? Color.WHITE : applyAlpha(getIncidentColor(incident), 0.95f);
		
		writer.println(TB0 + "<div style='height:" + PADDING + "px'></div>");
		
		writer.println(TB0 + "<div style='margin-left:" + INDENT + "px; border: 1px solid #d3d3d3; background-color:#" + getHex(backgroundColor) + "; border-radius: " + BORDER_RADIUS + "px;padding: " + PADDING + "px;'>");				
		writer.println(TB0 + "<div style='overflow:auto;'>");
		
			writer.println(TB1 +"<code><p style='white-space:nowrap;'>");
			writer.println(TB2 +
					"<span style='color:#" + getHex(incidentColor) + "; font-weight:bold;'>" +
					(hasChildren 
						? "<span style='cursor:pointer;' onclick='toggleById(" + (incidentId + 0) + ");'><u>"
						: ""
					) +
					
					"<b>" + htmlSafeString(type) + "</b>" +
					
					(hasChildren 
						? "</u></span>" 
						: "") +
					"</span>" +
					
					": " + message);
			
			if (hasAttachments)
			{
				writer.println(TB2 + "<span style='cursor:pointer;font-weight:bold; color:#d3d3d3;' onclick='toggleById(" + (incidentId + 1) + ");'>");
				writer.println(TB2 + "Attachments");
				writer.println(TB2 + "</span>");
				
				writer.println(TB2 + "<div class='attachment' id='" + (incidentId + 1) + "' style='display:none; margin-left:" + INDENT + "px';>");
				for (int i = 0; i < attachments.length; i++)
				{
					writer.println("<br>");
					this.writeAttachment(attachments[i]);
				}
				writer.println(TB2 + "</div>");
			}
			
			writer.println(TB1 + "</p></code>");			
			
			if (hasChildren)
			{
				writer.println(TB2 + "<div class='child' id='" + (incidentId + 0) + "' style='display:none;'>");				
				List<Incident> children = incident.getChildren();
				for (Incident child : children)
				{					
					this.processIncident(child);
				}
				writer.println(TB2 + "</div>");
			}
		
		writer.println(TB0 + "</div>");			
		writer.println(TB0 + "</div>");
		
		writer.flush();				
		
		if (incident.getLevel() == 0 && this.incidentId > PAGE_SIZE)
		{
			this.cutoverTrace();
		}		
	}
	
	private void writeThrowable(Throwable e)
	{
		writer.println(e.toString());
				
		for (StackTraceElement element : e.getStackTrace())
		{
			writer.println(element.toString());
		}
		
		Throwable cause = e.getCause();
		
		if (cause != null)
		{
			writer.println();
			writer.println("CAUSE");
			this.writeThrowable(cause);;
		}
	}
	
	private void writeAttachment(Object attachment)
	{
		attachmentId++;
		writer.println(TB0 + "<div style='margin-left:" + INDENT + "px; border: 1px solid #d3d3d3; background-color:#ffffff; border-radius: " + BORDER_RADIUS + "px;padding: " + PADDING + "px;'>");		
		writer.println(TB0 + "<p><b>" + attachment.getClass().getSimpleName() + "</b></p>");
		writer.println(TB1 + "<div style='margin-left:" + INDENT + "px;'>");
		
		if (attachment instanceof StackTraceElement[])
		{
			writer.println(TB1 + "<code><pre>");
			StackTraceElement[] stacktrace = (StackTraceElement[]) attachment;
			for (StackTraceElement element : stacktrace)
			{
				writer.println(element.toString());
			}
			writer.println(TB1 + "</pre></code>");
		}
		if (attachment instanceof Throwable)
		{
			writer.println(TB1 + "<code><pre>");
			
			this.writeThrowable((Throwable) attachment);
			
			writer.println(TB1 + "</pre></code>");
		}		
		else if (attachment instanceof Stack)
		{
			writer.println(TB1 + "<code><pre>");
			Stack stack = (Stack) attachment;
			Iterator iterator = stack.iterator();
			while (iterator.hasNext())
			{				
				Object item = iterator.next();
				if (item == null)
				{
					continue;
				}
				
				String itemStr = OpenGLManager.getInstance().getShortDescription(item);
				writer.println(itemStr);
			}
			writer.println(TB1 + "</pre></code>");
		}
		else if (attachment instanceof String[])
		{
			writer.println(TB1 + "<code><pre>");
			String[] text = (String[]) attachment;
			for (String line : text)
			{
				String itemStr = OpenGLManager.getInstance().getShortDescription(line);
				writer.println(itemStr);
			}
			writer.println(TB1 + "</pre></code>");
		}
		else if (attachment instanceof BufferedImage[])
		{
			BufferedImage[] images = (BufferedImage[]) attachment;
			
			for (int i = 0; i < images.length; i++)
			{
				BufferedImage image = images[i];
				
				String filename = "Resources" + File.separator + "image_" + attachmentId + "_" + i + ".png";
				try
				{
					ImageIO.write(image, "PNG", new File("trace" + File.separator + filename));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				
				float scale = 0.5f;
				writer.println(TB1 + "<img src=\"" + filename + "\" style=\"width:" + (int) (image.getWidth()*scale) + "px;height:" + (int) (image.getHeight()*scale) + "px;\">");
			}
		}
		
		writer.println(TB1 + "</div>");
		writer.println(TB0 + "</div>");
	}
}
