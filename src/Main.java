import java.io.*;
import java.util.*;
import java.lang.*;

public class Main
{
	public static void main(String[] args) throws IOException
	{
		// open input and output file
		File file = new File(args[0]);
                BufferedReader br = new BufferedReader(new FileReader(file));
                PrintStream ps = new PrintStream(new File("output.txt"));
		System.setOut(ps);	// System will print out to the output file
		
		// declare current process
		PCB current;

		// initialize resource manager
		ResourceManager RM = new ResourceManager();
		
		// initialize ready list
		ReadyList RL = new ReadyList();
		
		// create initial process and add it to ready list
		PCB init = new PCB("init", null, "running", 0);
		current = init;
		RL.add(0, init);
		System.out.print("init ");

		// Read in input file
		String s;
		String[] words;

		while ((s = br.readLine()) != null)
		{
			words = s.split(" ");	// Parse line into tokens
			
			if (words[0].equals("init"))
			{
				RM = new ResourceManager();
				RL = new ReadyList();
				current = init;
				RL.add(0, init);
				System.out.print("init ");
			}
			else if (words[0].equals("cr"))
			{
				// Create a new process

				int prio = Integer.parseInt(words[2]);
				PCB new_process = new PCB(words[1], current, "ready", prio);
				new_process.parent = current;
				RL.add(prio, new_process);	// Add new process to ready/running list
				//current.children.add(new_process);	// Add new process to parent's children list
				
				// Add new process to all ancestors' children list
				PCB temp = current.parent;
				while (temp != null)
				{
					temp.children.add(new_process);
					temp = temp.parent;			
				}

				PCB to_run = RL.Scheduler(current);	// Update current running process
				current = to_run;
				
				System.out.print(current.pid + " ");
			}
			else if (words[0].equals("de"))
			{
				// Destroy a process

				boolean destroyed = RL.destroy(current, RM, words[1], RL);
				if (!destroyed)
				{
					System.out.print("error ");
				}
				else
				{
					if (current.pid.equals(words[1]))	// Destroy current running process
					{
						PCB temp = new PCB("temp", null, "ready", -1);
						current = temp;
					}

					PCB to_run = RL.Scheduler(current);	// Update current running process
					current = to_run;

					System.out.print(current.pid + " ");
				}
						
			}
			else if (words[0].equals("req"))
			{
				// Request resources for current running process

				int resource_number = Character.getNumericValue(words[1].charAt(1));
				int number_of_resources = Integer.parseInt(words[2]);
			
				if (current.priority > 0 && number_of_resources > resource_number)
				{
					System.out.print("error ");	// Error if request is more than available
				}
				else
				{
					RM.request(current, resource_number, number_of_resources, RL);

					PCB to_run = RL.Scheduler(current);	// Update current running process
					current = to_run;

					System.out.print(current.pid + " ");
				}
			}
			else if (words[0].equals("rel"))
			{	
				// Release resources for a current running process

				int resource_number = Character.getNumericValue(words[1].charAt(1));
                                int number_of_resources = Integer.parseInt(words[2]);

				if (current.priority > 0 && number_of_resources > resource_number)
				{
					System.out.print("error ");	// Error if release is more than available

				}
				else
				{
					RM.release(current, resource_number, number_of_resources, RL);

					PCB to_run = RL.Scheduler(current);	// Update current running process
					current = to_run;

					System.out.print(current.pid + " ");
				}
			}
			else if (words[0].equals("to"))
			{
				// Time out the current running process

				RL.time_out(current);

				PCB to_run = RL.Scheduler(current);	// Update current running process
				current = to_run;

				System.out.print(current.pid + " ");
			}
			else if (words[0].equals("") || words[0].equals("\n"))
			{
				System.out.println();
			}
			else
			{
				System.out.print("error ");
			}
		}
	}
}
