#created by liyr on 6/8/17#
import random

global it
global clock
lis = [10.,6.,8.,0.,0.,0.,0.,0.]
mem = [0] * 1000

def randInst(f1,f2):
    global it
    global clock
    a = random.randint(0,7)
    b = random.randint(0,7)
    c = random.randint(0,7)
    d = random.randint(0,30)
    e = random.randint(0,it)
    if d <= 10:
        lis[a] = lis[b] + lis[c]
        mem[it] = lis[a]
        f1.write("ADDD F%d,F%d,F%d\n"%(a,b,c))
        f1.write("ST F%d,%d\n"%(a,it))
        f2.write("%d : %f\n"%(it, lis[a]))
        it = it + 1
        clock = clock + 4
    elif d <= 20:
        lis[a] = lis[b] - lis[c]
        mem[it] = lis[a]
        f1.write("SUBD F%d,F%d,F%d\n"%(a, b, c))
        f1.write("ST F%d,%d\n"%(a,it))
        f2.write("%d : %f\n"%(it, lis[a]))
        it = it + 1
        clock = clock + 4
    elif d <= 22 or (d <= 24 and lis[c] == 0.):
        lis[a] = lis[b] * lis[c]
        mem[it] = lis[a]
        f1.write("MULTD F%d,F%d,F%d\n"%(a, b, c))
        f1.write("ST F%d,%d\n"%(a,it))
        f2.write("%d : %f\n"%(it, lis[a]))
        it = it + 1
        clock = clock + 10
    elif d <= 24 :
        lis[a] = lis[b] / lis[c]
        mem[it] = lis[a]
        f1.write("DIVD F%d,F%d,F%d\n"%(a, b, c))
        f1.write("ST F%d,%d\n"%(a,it))
        f2.write("%d : %f\n"%(it, lis[a]))
        it = it + 1
        clock = clock + 40
    else:
        lis[a] = mem[e - 17]
        f1.write("LD F%d,%d\n"%(a, e))
        clock = clock + 2




if __name__ == "__main__":
    it = 17
    clock = 0
    with open('testCase.txt', 'w') as f1:
        with open('ans.txt', 'w') as f2:
            f1.write("LD F0,0\nLD F1,4\nLD F2,16\n")
            clock = clock + 6
            for x in range(50):
                randInst(f1,f2)
            f2.write("clock = %d\n"%(clock))
    f1.close()
    f2.close()
