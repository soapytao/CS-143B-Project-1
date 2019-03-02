import java.io.*;
import java.util.*;
import java.lang.*;

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
