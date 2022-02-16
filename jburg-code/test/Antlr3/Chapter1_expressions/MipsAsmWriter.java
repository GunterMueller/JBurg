import java.io.PrintWriter;

class MipsAsmWriter
{
	private PrintWriter asm_out;

	private static int START = 0;
	private static int DATA_SECTION  = 1;
	private static int TEXT_SECTION  = 2;

	private int m_section;

	public MipsAsmWriter(PrintWriter out)
	{
		asm_out = out;
		m_section = START;
	}

	public void printInstruction(String opcode, Object dst, Object operand1, Object operand2)
	{
		setSection(TEXT_SECTION);

		asm_out.print("\t");
		asm_out.print(opcode);
		asm_out.print("\t");
		asm_out.print(dst.toString());

		if ( operand1 != null )
		{
			asm_out.print(",\t" + operand1.toString());
		}

		if ( operand2 != null )
		{
			asm_out.print(",\t" + operand2.toString());
		}

		asm_out.println();
	}

	public void enterProcedure(String procedureName)
	{
		setSection(TEXT_SECTION);
		asm_out.println(procedureName + ":");
	}

	public void exitProcedure()
	{
		asm_out.println("\tjr\t$ra");
	}

	public void printAddressInstruction(String opcode, Object reg, Object addr)
	{
		setSection(TEXT_SECTION);

		asm_out.print("\t" + opcode + "\t");
		asm_out.print(reg.toString() + ",\t");
		asm_out.println("(" + addr.toString() + ")");
	}

	public void printDecl(Object type, Object sym)
	{
		setSection(DATA_SECTION);

		asm_out.println( sym.toString() + ":\t.word\t0");
	}

	void setSection(int section)
	{
		if ( m_section != section )
		{
			if ( DATA_SECTION == section )
			{
				asm_out.println("\t.data");
			}
			else if ( TEXT_SECTION == section )
			{
				asm_out.println("\t.text");
			}
			else
			{
				throw new IllegalStateException("Unknown section " + Integer.toString(section));
			}

			m_section = section;
		}
	}

}
