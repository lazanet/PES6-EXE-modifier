//package editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.filechooser.*;
import java.io.*;

public class EXEmodifier extends JFrame
{
	byte[] content;

	// online server
	static int PES6GATE = 0x7b48c4;
	static int MAX_PES6GATE = 30;
	static int WE9STUN = 0x7b5a1c;
	static int MAX_WE9STUN = 26;

	//years
	static int[] YEAR_OFFSETS = {0x2ea25b, // main year
				   0x2ef17e, // evolution
				   0x2ef184, // join club
				   0x33f40e, // seasons results
				   0x724795, // save
				   0x2e7ff6}; // my best eleven

	static int SAVE_FOLDER = 0x77df91;
	static int MAX_SAVE_FOLDER = 7;

	static int CAMERA_OFFSET = 0x46b0cd;
	
	static int[] CAMERA_ROUTINES = {0x563c3d,
					0x563cd1,
					0x563d22,
					0x563d39,
					0x563d4e,
					0x563f6c,
					0x563fcb,
					0x563fe9,
					0x564050,
					0x56406e,
					0x56406f,
					0x564070,
					0x564071,
					0x564073};

	static int[] CAMERA_ROUTINE_VALUES = {0x7a, 0x7a, 0x75, 0x7a, 0x75, 0x7a, 0x75, 0x75, 0x75, 0x0f, 0x8a, 0xe8, 0xfc, 0xff};
	
	static int unsb(byte a)
	{
		int b = a & 0xFF;
		return b;
	}

	public float findCameraDistance()
	{
		int distTmp = 0;
		for (int i=3; i>=0; i--)
			distTmp = distTmp<<8 | unsb(content[CAMERA_OFFSET+i]);
		return Float.intBitsToFloat(distTmp);
	}
	public void patchCameraRoutines()
	{
		for (int i=0; i<CAMERA_ROUTINES.length; i++)
			content[CAMERA_ROUTINES[i]] = (byte)(CAMERA_ROUTINE_VALUES[i] & 0xff);
	}
	public void setCameraDistance(float a)
	{
		patchCameraRoutines();
		int distTmp = Float.floatToIntBits(a);
		for (int i=0; i<=3; i++)
		{	
			content[CAMERA_OFFSET+i] = (byte)(distTmp & 0xFF);
			distTmp = distTmp>>8;
		}
	}
	public String findString(int offset, int maxLen)
	{
		String tmp = "";
		for(int i=0; (i<maxLen || (content[offset+i]!=0x0)); i++)
			tmp+=(char)content[offset+i];
		return tmp;
		
	}
	public void setString(int offset, int maxLen, String value)
	{
		int i;
		for(i=0; (i<maxLen && i<value.length()); i++)
			content[offset+i] = (byte)(value.charAt(i));
		for (; i<maxLen; i++)
			content[offset+i]='\0';
		
	}
	public void loadFile(String s)
	{
		try
		{
			RandomAccessFile f = new RandomAccessFile(s,"r");
			content = new byte[(int)(f.length())];
			f.readFully(content);
		}
		catch(Exception e)
		{
			System.out.println("Exception: "+e);
		}	
	}

	public void saveFile(String fName)
	{
		try (FileOutputStream fos = new FileOutputStream(fName)) 
		{
   			fos.write(content);
   			fos.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	public int findYear()
	{
		return unsb(content[YEAR_OFFSETS[0]]) | unsb(content[YEAR_OFFSETS[0]+1])<<8;
	}
	public int setYear(int year)
	{
		if ((year < 0) || (year>0xFFFF)) return -1;
		for (int i=0; i<YEAR_OFFSETS.length; i++)
		{
			content[YEAR_OFFSETS[i]] = (byte)(year & 0xFF);
			content[YEAR_OFFSETS[i]+1] = (byte)((year>>8) & 0xFF);
		}
		return 0;
	}	

	public void close()
	{
		System.exit(0); 
		//dispose();
	}
	public void error(String s)
	{
		JOptionPane.showMessageDialog(this, s, "Error", JOptionPane.ERROR_MESSAGE);
	}
	public void info(String s)
	{
		JOptionPane.showMessageDialog(this, s);
	}

	TextField TPes6Gate;
	Label LPes6Gate;
	Label LWe9Stun;
	TextField TWe9Stun;
	Label Lyear;
	TextField Tyear;
	JButton Bsubmit;
	TextField TsaveLocation;
	Label LsaveLocation;
	
	public EXEmodifier()
	{
		super("PES6-EXE-modifier");

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(File.listRoots()[0].getAbsolutePath()));
		fileChooser.setDialogTitle("Select PES6.exe");
		fileChooser.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Select PES6.exe", "exe");
		fileChooser.addChoosableFileFilter(filter);

		int result = fileChooser.showOpenDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) // if user canceled filechooser close
			close();
		File tmp = fileChooser.getSelectedFile();

		loadFile(tmp.getAbsolutePath());
		try
		{
			// DRAW FORM!
			setLayout(null);

			LPes6Gate = new Label();
			LPes6Gate.setLocation(20,25);
			LPes6Gate.setSize(120,25);
			LPes6Gate.setText("PES6GATE server:");
			add(LPes6Gate);

			TPes6Gate = new TextField();
			TPes6Gate.setLocation(150,25);
			TPes6Gate.setSize(200,25);
			TPes6Gate.setBackground( new Color(-1) );
			TPes6Gate.setText(findString(PES6GATE, MAX_PES6GATE));
			TPes6Gate.setColumns(10);
			add(TPes6Gate);

			LWe9Stun = new Label();
			LWe9Stun.setLocation(20,70);
			LWe9Stun.setSize(110,25);
			LWe9Stun.setText("WE9STUN server: ");
			add(LWe9Stun);

			TWe9Stun = new TextField();
			TWe9Stun.setLocation(150,69);
			TWe9Stun.setSize(200,25);
			TWe9Stun.setBackground( new Color(-1) );
			TWe9Stun.setText(findString(WE9STUN, MAX_WE9STUN));
			TWe9Stun.setColumns(10);
			add(TWe9Stun);

			Lyear = new Label();
			Lyear.setLocation(20,110);
			Lyear.setSize(120,25);
			Lyear.setText("ML Start Year:");
			add(Lyear);

			Tyear = new TextField();
			Tyear.setLocation(150,108);
			Tyear.setSize(71,25);
			Tyear.setBackground( new Color(-1) );
			Tyear.setText(Integer.toString(findYear()));
			Tyear.setColumns(10);
			add(Tyear);


			LsaveLocation = new Label();
			LsaveLocation.setLocation(20,152);
			LsaveLocation.setSize(120,25);
			LsaveLocation.setText("OPT file location:");
			add(LsaveLocation);

			TsaveLocation = new TextField();
			TsaveLocation.setLocation(150,150);
			TsaveLocation.setSize(200,25);
			TsaveLocation.setBackground( new Color(-1) );
			TsaveLocation.setText(findString(SAVE_FOLDER, MAX_SAVE_FOLDER));
			TsaveLocation.setColumns(10);
			add(TsaveLocation);
			
			Bsubmit = new JButton();
			Bsubmit.setLocation(22,204);
			Bsubmit.setSize(320,25);
			Bsubmit.setText("MODIFY EXE");
			add(Bsubmit);
			Bsubmit.addActionListener(new ActionListener() 
			{ 
  				public void actionPerformed(ActionEvent e)
				{ 
					int year;
					try
					{
						year = Integer.parseInt(Tyear.getText());
					}
					catch(Exception ex)
					{
						error("Year is not a number!");
						return;
					}
					if (setYear(year) == -1)
					{
						error("Year is not in range!");
						return;
					}
    					setString(PES6GATE, MAX_PES6GATE, TPes6Gate.getText());
					setString(WE9STUN,  MAX_WE9STUN,  TWe9Stun.getText());
					setString(SAVE_FOLDER, MAX_SAVE_FOLDER, TsaveLocation.getText());	
					info("DONE!");
					saveFile(tmp.getAbsolutePath());
				} 
			});

			setSize(370,300);
			setResizable(false);
			setVisible(true);
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		}
		catch(Exception e)
		{
			error("Not valid or damaged PES6.exe. Exiting... \nError: "+e);
			close();
		}		
		

		/*loadFile("oldTrusty.exe");
		System.out.println("First year: "+findYear());
		//setYear(2048);
		System.out.println("First year: "+findYear());
		System.out.println("");
		System.out.println("Camera distance: "+findCameraDistance());
		setCameraDistance(1450);
		System.out.println("Camera distance: "+findCameraDistance());
		System.out.println("");
		System.out.println("PES6GATE: " + findString(PES6GATE, MAX_PES6GATE));
		//setString(PES6GATE, MAX_PES6GATE, "127.0.0.1");
		System.out.println("PES6GATE: " + findString(PES6GATE, MAX_PES6GATE));
		System.out.println("WE9STUN: " +  findString(WE9STUN, MAX_WE9STUN));	
		//setString(PES6GATE, MAX_PES6GATE, "192.168.0.196");
		saveFile("NEWPES6Test.exe"); */
	}
	public static void main(String[] args)
	{
		new EXEmodifier();
	}
	
}
