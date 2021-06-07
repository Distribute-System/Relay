package carDataShare;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

public class LinkedListQueue<T> implements Queue<T> 
{
	
	public static void main(String args[])
	{
		LinkedListQueue<String> aQu = new LinkedListQueue<String>();
		aQu.add("Hello");
		if(aQu.isEmpty())
		{
			System.out.println("1 queue is empty!");
		}
		String temp = aQu.remove();
		if(aQu.isEmpty())
		{
			System.out.println("2 queue is empty!");
		}
	}
	
	Node<T> current;
	Node<T> nullNode;
	
	
	LinkedListQueue()
	{
		nullNode = new Node<T>(null, null);
		current = nullNode;
	}
	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return nullNode == current;
	}

	@Override
	public boolean contains(Object o) {
		Node<T> temp = current;
		
		
		while(!this.isEmpty())
		{
			if(temp.element.equals(o))
			{
				return true;
			}
			
			temp = current.next;
		}
		
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object o) 
	{		
		if(current == nullNode)
			return false;
		
		current = current.next;
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		
		for(Object temp: c)
		{
			if(!contains(temp))
			{
				return false;
			}
		}
			
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		
		for(Object temp: c)
		{
			add((T)temp);
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean add(T e) 
	{
		
		current.next = new Node<T>(e, null);
		current = current.next;
		return true;
	}

	@Override
	public boolean offer(T e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T remove() {
		if(current == nullNode)
			return null;
		
		T temp = nullNode.next.element;
		if(current == nullNode.next)
		{
			current = nullNode;
		}
		
		nullNode.next = nullNode.next.next;
		return temp;
		
	}

	@Override
	public T poll() {
		// TODO Auto-generated method stub
		return remove();
	}

	@Override
	public T element() {
		// TODO Auto-generated method stub
		return remove();
	}

	@Override
	public T peek() {
		// TODO Auto-generated method stub
		return current.element;
	}
	
	static class Node <T>
	{
		T element;
		Node<T> next;
		
		Node(T el, Node<T> next)
		{
			element = el;
			this.next = next;
		}

	}

}


