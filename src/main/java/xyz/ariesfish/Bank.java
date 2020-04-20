package xyz.ariesfish;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Bank {
    private final double[] accounts;
    private Lock bankLock;
    private Condition sufficientFunds;

    public Bank(int n, double initialBalance) {
        accounts = new double[n];
        Arrays.fill(accounts, initialBalance);
        bankLock = new ReentrantLock(); // 使用Lock控制转账操作
        sufficientFunds = bankLock.newCondition(); // 使用条件对象判断是否有充足的余额转账
    }

    public void transfer(int from, int to, double amount) {
        bankLock.lock();
        try {
            while (accounts[from] < amount) {
                sufficientFunds.await(); // 如果余额不足则等待, 并放弃锁
            }
            System.out.print(Thread.currentThread());
            accounts[from] -= amount;
            System.out.printf(" %10.2f from %d to %d", amount, from, to);
            accounts[to] += amount;
            System.out.printf(" Total Balance: %10.2f%n", getTotalBalance());

            sufficientFunds.signalAll(); // 通知等待中的线程, 解除它们的阻塞状态
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace());
        } finally {
            bankLock.unlock();
        }
    }

    public double getTotalBalance() {
        bankLock.lock();
        try {
            double sum = 0;
            for (double a : accounts)
                sum += a;

            return sum;
        } finally {
            bankLock.unlock();
        }
    }

    public int size()
    {
        return accounts.length;
    }
}
