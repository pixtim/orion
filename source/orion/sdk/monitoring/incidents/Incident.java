package orion.sdk.monitoring.incidents;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import orion.sdk.monitoring.performance.CallStack;
import orion.sdk.util.IPersistable;
import orion.sdk.util.StructuredBinary;

public class Incident implements IPersistable
{
	private static int nextID = 0;
	
	protected int type = Type.INFORMATION;
	protected int level;
	protected String message;
	protected Object[] attachments;
	protected int id = 0;
	protected List<Incident> children;
	protected Incident parent;

	public Incident(int type, String message, Object... attachments)
	{
		this.id = Incident.nextID++;
		this.type = type;
		this.message = message;
		this.attachments = attachments;
		this.level = IncidentManager.getCurrentLevel();
		this.children = new LinkedList<Incident>();
		
		Incident currentParent = IncidentManager.getCurrentParent();
		if (currentParent != null)
		{
			this.parent = currentParent;
			this.parent.addChild(this);
		}
	}
	
	public void addChild(Incident child)
	{
		this.children.add(child);
	}
	
	public List<Incident> getChildren()
	{
		return children;
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public int getId()
	{
		return id;
	}
	
	public int getType()
	{
		return type;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public Object[] getAttachments()
	{
		return attachments;
	}

	public static Incident newError(String message, Throwable e)
	{
		e.printStackTrace();
		
		return newError(message, new Object[] {e});
	}
	
	public static Incident newError(String message, Object... attachments)
	{
		return new Incident(Type.ERROR, message, attachments);
	}
	
	public static Incident newInformation(String message, Object... attachments)
	{
		return new Incident(Type.INFORMATION, message, attachments);
	}
	
	public static Incident newWarning(String message, Object... attachments)
	{
		return new Incident(Type.WARNING, message, attachments);
	}
	
	public static Incident newDebug(String message, Object... attachments)
	{
		return new Incident(Type.DEBUG, message, attachments);
	}
	
	public static Incident newError(String message, CallStack trace)
	{
		return new Incident(Type.ERROR, message, new Object[] {trace.stackTrace});
	}
	
	public static Incident newInformation(String message, CallStack trace)
	{
		return new Incident(Type.INFORMATION, message, new Object[] {trace.stackTrace});
	}
	
	public static Incident newWarning(String message, CallStack trace)
	{
		return new Incident(Type.WARNING, message, new Object[] {trace.stackTrace});
	}
	@Override
	public void read(StructuredBinary binary) throws Exception
	{
		type = binary.getInt(0);
		message = binary.getString(1);
		attachments = new String[binary.getInt(2)];
		for (int i = 0; i < attachments.length; i++)
		{
			attachments[i] = binary.getString(i + 3);
		}
	}

	@Override
	public StructuredBinary write() throws Exception
	{
		StructuredBinary binary = new StructuredBinary();
		binary.add(type);
		binary.add(message);
		binary.add(attachments.length);
		for (Object attachment : attachments)
		{
			binary.add(attachment.toString());
		}
		return binary;
	}

	@Override
	public String toString()
	{
		String sep = System.lineSeparator();		
		String text = Type.toString(type) + ": " + sep;
		text = text + "   " + message + sep;
		if (attachments != null && attachments.length > 0)
		{
			text = text + "Attachments:" + sep;
			for (int i = 0; i < attachments.length; i++)
			{
				String line = "   " + attachments[i].toString();
				text = text + line + sep;
			}
		}
		return text.trim();
	}
	
	public boolean hasContent()
	{
		List<Incident> children = this.getChildren();
		
		if (children.isEmpty())
		{
			return this.getType() != Type.DEBUG;
		}
		else
		{
			for (Incident child : children)
			{
				if (child.hasContent())
				{
					return true;
				}
			}
			
			return false;
		}
	}

	public static class Type
	{
		public static final int INFORMATION = 0;
		public static final int ERROR = 1;
		public static final int WARNING = 2;
		public static final int DEBUG = 3;
		public static final int OPENGL = 4;
		
		public static String toString(int type)
		{
			switch (type)
			{
				case INFORMATION:
					return "Information";
				case ERROR:
					return "Error";
				case WARNING:
					return "Warning";
				case DEBUG:
					return "Debug";
				case OPENGL:
					return "OpenGl";
				default:
					return "Unknown";
			}
		}
	}
}
