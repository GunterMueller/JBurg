interface RegisterAllocator
{
	/**
	 *   @return a register to hold the given symbol.
	 *   @pre isInRegister(sym) is false.
	 *   @post isInRegister(sym) is true.
	 */
	public Object allocateRegister(Object sym);

	public boolean isInRegister(Object sym);

	public boolean hasFreeRegister();

	public Object spillRegister(Object new_sym);
}
