import java.io.*;
import java.util.*;
import java.lang.*;

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
		rnum -= 1;	// Subtract 1 for indexing
		if (resource_list[rnum].current_free >= quantity)	// if avail >= requested, give resources
		{
			pr.resources[rnum] += quantity;
			resource_list[rnum].current_free -= quantity;
		}
		else	// if avail < requested, block process and add to waiting list
		{
			pr.status = "blocked";
			rl.remove(pr.priority, pr);
			pr.req_blocked = quantity;
			resource_list[rnum].waiting_list.add(pr);
		}
	}
	
	void release(PCB pr, int rnum, int quantity, ReadyList rl)
	{
		rnum -= 1;	// Subtract 1 for indexing

		// Release the resources
		pr.resources[rnum] -= quantity;
		resource_list[rnum].current_free += quantity;
		
		// Iteratively find waiting processes that can use the newly freed resources
		ListIterator<PCB> iter = resource_list[rnum].waiting_list.listIterator();
		while (iter.hasNext())
		{
			PCB temp = iter.next();
			if (resource_list[rnum].current_free >= temp.req_blocked)
			{
				resource_list[rnum].current_free -= temp.req_blocked;	// Update resource list
				temp.resources[rnum] += temp.req_blocked;	// Update process' resources
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
		rnum -= 1;	// Subtract 1 for indexing
		resource_list[rnum].waiting_list.remove(pr);
	}
				
}
