int factorial(int x)
{
    if (x == 1) {
        return 1;
    } else {
        return factorial(x-1) * x;
    }
}

int main()
{
    print("factorial 5:", factorial(5));
    return 0;
}
