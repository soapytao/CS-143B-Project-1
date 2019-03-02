import java.io.*;
import java.util.*;
import java.lang.*;

class ReadyList
{
	/* The ready list is a map where keys are priorities 0-2 and 
	// values are lists of "ready" processes with the respective priorities.
	// Processes are in FIFO order. */

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

	// get the highest priority process that is first in line
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

	// Called after every command to update the process which should be currently running
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

	// Destroys a process and all its descendants, removes them from ready list 
	boolean destroy(PCB cur, ResourceManager rm, String destroy_pid, ReadyList rl)
	{
		boolean found_child = false;
		if (destroy_pid == "init")
		{
			return found_child;
		}
	
		// if the process to be destroyed is the current one
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
		
		// otherwise search for the process in the current process' descendants
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
	
	// Recursively kills a parent's descendant processes
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
	
	// Time out the currently running process and queue it back into the ready list
	void time_out(PCB cur)
	{
		cur.status = "ready";
		remove(cur.priority, cur);
		add(cur.priority, cur);
	}
			
}
