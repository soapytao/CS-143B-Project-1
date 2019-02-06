import java.io.*;
import java.util.*;
import java.lang.*;

class PCB
{
	String pid;
	int resources[];
	PCB parent;
	LinkedList<PCB> children;
	String status;
	int priority;
	int req_blocked;
	

	PCB(String id, PCB par, String s, int p)
	{
		this.pid = id;
		resources = new int[4];
		for (int i = 0; i < 4; ++i)
		{
			resources[i] = 0;
		}
		this.parent = par;
		this.children = new LinkedList<PCB>();
		this.status = s;
		this.priority = p;
		this.req_blocked = 0;
	}
}

class RCB
{
	String rid;
	int initial_free;
	int current_free;
	LinkedList<PCB> waiting_list;
	
	RCB(String id, int num_resources)
	{
		this.rid = id;
		this.initial_free = num_resources;
		this.current_free = num_resources;
		this.waiting_list = new LinkedList<PCB>();
	}
		
}

class ResourceManager
{
	RCB resource_list[];
	
	ResourceManager()
	{
		resource_list = new RCB[4];
		for (int i = 0; i < 4; ++i)
		{
			String name = "R" + Integer.toString(i+1);
			resource_list[i] = new RCB(name, i+1);
		}
	}
	
	void request(PCB pr, int rnum, int quantity, ReadyList rl)
	{
		rnum -= 1;
		if (resource_list[rnum].current_free >= quantity)
		{
			pr.resources[rnum] += quantity;
			resource_list[rnum].current_free -= quantity;
		}
		else
		{
			pr.status = "blocked";
			rl.remove(pr.priority, pr);
			pr.req_blocked = quantity;
			resource_list[rnum].waiting_list.add(pr);
		}
	}
	
	void release(PCB pr, int rnum, int quantity, ReadyList rl)
	{
		rnum -= 1;
		pr.resources[rnum] -= quantity;
		resource_list[rnum].current_free += quantity;
		
		ListIterator<PCB> iter = resource_list[rnum].waiting_list.listIterator();
		while (iter.hasNext())
		{
			PCB temp = iter.next();
			if (resource_list[rnum].current_free >= temp.req_blocked)
			{
				resource_list[rnum].current_free -= temp.req_blocked;
				temp.resources[rnum] += temp.req_blocked;
				//resource_list[rnum].waiting_list.remove(temp);
				//iter.remove();
				temp.status = "ready";
				rl.add(temp.priority, temp);
				iter.remove();
			}
		}
	}
	
	void dequeue(PCB pr, int rnum)
	{
		rnum -= 1;
		resource_list[rnum].waiting_list.remove(pr);
	}

				
			
				
}

class ReadyList
{
	HashMap<Integer,LinkedList<PCB>> ready_list;
	
	ReadyList()
	{
		this.ready_list = new HashMap<Integer, LinkedList<PCB>>();
		for (int i = 0; i < 3; ++i)
		{
			ready_list.put(i, new LinkedList<PCB>());
		}
	}

	void printRL()
	{
		for (int i = 0; i < 3; ++i)
		{
			System.out.print(i);
			System.out.print(" ");
			if (!ready_list.get(i).isEmpty())
			{
				System.out.println(ready_list.get(i).getFirst().pid);
			}
		}
	}
	
	void add(int key, PCB value)
	{
		ready_list.get(key).add(value);
	}

	void remove(int key, PCB value)
	{
		ready_list.get(key).remove(value);
	}

	PCB get_highest()
	{
		for (int i = 2; i >= 0; --i)
		{
			if (!ready_list.get(i).isEmpty())
			{
				return ready_list.get(i).getFirst();
			}
		}
		return null;
	}

	PCB Scheduler(PCB cur)
	{
		PCB highest = get_highest();
		if (cur.priority < highest.priority ||	// called from create / release
			!cur.status.equals("running") ||	// called from request / time-out
			cur.priority == -1)	// called from destroy
		{
			highest.status = "running";
			return highest;
		}
		else
		{
			return cur;
		}
	}

	boolean destroy(PCB cur, ResourceManager rm, String destroy_pid, ReadyList rl)
	{
		if (destroy_pid == "init")
		{
			return false;
		}
		boolean found_child = false;
		if (cur.pid.equals(destroy_pid))
		{
			kill_children(cur, rm, rl);
                        rl.remove(cur.priority, cur);
                        for (int i = 0; i < 4; ++i)
                        {
                        	rm.dequeue(cur, i+1);
                                rm.release(cur, i+1, cur.resources[i], rl);
                        }
			found_child = true;
		}
		else
		{
			ListIterator<PCB> iter = cur.children.listIterator();
			while (iter.hasNext())
			{
				PCB temp = iter.next();
				if (temp.pid.equals(destroy_pid))
				{
					kill_children(temp, rm, rl);
					rl.remove(temp.priority, temp);
					for (int i = 0; i < 4; ++i)
					{
						rm.dequeue(temp, i+1);
						rm.release(temp, i+1, temp.resources[i], rl);
					}
					iter.remove();
					found_child = true;
				}
			}
		}
		return found_child;
		
	}
	
	void kill_children(PCB par, ResourceManager rm, ReadyList rl)
	{
		ListIterator<PCB> iter = par.children.listIterator();
		while (iter.hasNext())
		{
			PCB temp = iter.next();
			kill_children(temp, rm, rl);
			iter.remove();
		}
		rl.remove(par.priority, par);
                for (int i = 0; i < 4; ++i)
                {
                	rm.dequeue(par, i+1);
                	rm.release(par, i+1, par.resources[i], rl);
                }

	}
			
	void time_out(PCB cur)
	{
		cur.status = "ready";
		remove(cur.priority, cur);
		add(cur.priority, cur);
	}
			
}

public class hw1
{
	public static void main(String[] args) throws IOException
	{
		// open input and output file
		File file = new File(args[0]);
                BufferedReader br = new BufferedReader(new FileReader(file));
                PrintStream ps = new PrintStream(new File("D:/20293428.txt"));
		System.setOut(ps);
		
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

		// parse input file
		String s;
		String[] words;

		while ((s = br.readLine()) != null)
		{
			words = s.split(" ");
			
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
				int prio = Integer.parseInt(words[2]);
				PCB new_process = new PCB(words[1], current, "ready", prio);
				new_process.parent = current;
				RL.add(prio, new_process);	// Add new process to ready/running list
				current.children.add(new_process);	// Add new process to parent's children list
				PCB temp = current.parent;
				while (temp != null)
				{
					temp.children.add(new_process);
					temp = temp.parent;			
				}

				PCB to_run = RL.Scheduler(current);
				current = to_run;
				
				System.out.print(current.pid + " ");
			}
			else if (words[0].equals("de"))
			{
				boolean destroyed = RL.destroy(current, RM, words[1], RL);
				if (!destroyed)
				{
					System.out.print("error ");
				}
				else
				{
					if (current.pid.equals(words[1]))
					{
						PCB temp = new PCB("temp", null, "ready", -1);
						current = temp;
					}
					PCB to_run = RL.Scheduler(current);
					current = to_run;
					System.out.print(current.pid + " ");
				}
						
			}
			else if (words[0].equals("req"))
			{
				int resource_number = Character.getNumericValue(words[1].charAt(1));
				int number_of_resources = Integer.parseInt(words[2]);
				if (current.priority > 0 && number_of_resources > resource_number)
				{
					System.out.print("error ");
				}
				else
				{
					RM.request(current, resource_number, number_of_resources, RL);
					PCB to_run = RL.Scheduler(current);
					current = to_run;
					System.out.print(current.pid + " ");
				}
			}
			else if (words[0].equals("rel"))
			{	
				int resource_number = Character.getNumericValue(words[1].charAt(1));
                                int number_of_resources = Integer.parseInt(words[2]);
				if (current.priority > 0 && number_of_resources > resource_number)
				{
					System.out.print("error ");
				}
				else
				{
					RM.release(current, resource_number, number_of_resources, RL);
					PCB to_run = RL.Scheduler(current);
					current = to_run;
					System.out.print(current.pid + " ");
				}
			}
			else if (words[0].equals("to"))
			{
				RL.time_out(current);
				PCB to_run = RL.Scheduler(current);
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
