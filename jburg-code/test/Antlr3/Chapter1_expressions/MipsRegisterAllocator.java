import java.security.SecureRandom;
import java.util.Vector;

class MipsRegisterAllocator
implements RegisterAllocator
{
	private Vector m_allocated_registers = new Vector(32);
	private Vector m_free_registers = new Vector(32);

	MipsRegisterAllocator()
	{
		//  Put the MIPS working registers in the free list.
		for ( int i = 8; i < 26; i++ )
		{
			m_free_registers.add(new Register(i));
		}
	}

	public boolean isInRegister(Object sym)
	{
		return m_allocated_registers.contains(sym);
	}

	public Object allocateRegister(Object sym)
	{
		Register retval = null;

		if ( hasFreeRegister() )
		{
			retval = (Register) m_free_registers.firstElement();
		}
		else
		{
			retval = (Register) spillRegister(sym);
		}

		retval.setSymbol(sym);
		m_free_registers.remove(retval);
		m_allocated_registers.add(retval);

		return retval;
	}

	public boolean hasFreeRegister()
	{
		return ! m_free_registers.isEmpty();
	}

	public Object spillRegister(Object new_sym)
	{
		if ( isInRegister(new_sym) )
		{
			throw new IllegalStateException(
				"Symbol " + 
				new_sym.toString() + 
				"already has a register."
			);
		}

		//  Simple round-robin register allocation.
		Register spillTarget = (Register) m_allocated_registers.firstElement();

		//  TODO: If this register holds the current value of a
		//  memory location, then generate a store instruction
		//  sequence to spill it.

		m_allocated_registers.remove(spillTarget);

		spillTarget.unsetSymbol();
		m_free_registers.add(spillTarget);

		return spillTarget;
	}

	class Register
	{
		private int m_regnum;
		private boolean m_is_floating_point;

		private Object m_associated_symbol = null;

		Register(int regnum)
		{
			m_regnum = regnum;
			m_is_floating_point = false;
		}

		public boolean equals(Object o)
		{
			if ( ! ( o instanceof Register ) )
			{
				return false;
			}

			Register other_reg = (Register) o;

			return m_associated_symbol.equals(other_reg.m_associated_symbol);
		}

		public String toString()
		{
			return "$" + Integer.toString(m_regnum);
		}

		void setSymbol(Object sym)
		{
			m_associated_symbol = sym;
		}

		void unsetSymbol()
		{
			m_associated_symbol = null;
		}

	}
}
