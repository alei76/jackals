package jackals.lab.shares;

import java.text.DecimalFormat;

public class Code {
    double start;
    double highest;
    double end;
    double lowest;
    double quantity;
    double money;
    double priceAvg;//当日均价
    double quantityAvg; //当日平均量
    double diff; //高低差
    String date;
    double c1; //7//均价变动
    double c2;//8//最高变动
    double c3;//9//最低变动
    double c4;//10 //高低差变动
    double c5;//11//量比
    double c6;//12//收盘5日涨幅
    double c7;//13//收盘10日涨幅
    double c8;//14//5日涨
    double c9;//15//10日涨 30%
    double c10;//16//30日涨
    double c11;//17 //月均价
    double c12;//18//月均价一日变动
    double c13;//19//月均价未来10日变动
    double c14;//20//月均量
    double c15;//21//月均量一日变动
    double c16;//22//月均量未来10日变动
    double c17;//23//均量量比
    boolean done = false;
    public Code(String s) {
        String[] arr = s.split("\\s");
        date = arr[0];//            日期
        start = Double.valueOf(arr[1]);//            开盘价
        highest = Double.valueOf(arr[2]);//            最高价
        end = Double.valueOf(arr[3]);//            收盘价
        lowest = Double.valueOf(arr[4]);//            最低价
        quantity = Double.valueOf(arr[5]);//            交易量(股)
        money = Double.valueOf(arr[6]);//            交易金额(元)
        priceAvg = (highest + lowest) / 2;
        quantityAvg = quantity;
        diff = Math.abs(highest - lowest);
    }

    public Code() {

    }

    public double getPriceAvg() {
        return priceAvg;
    }

    public void setPriceAvg(double priceAvg) {
        this.priceAvg = priceAvg;
    }

    public double getQuantityAvg() {
        return quantityAvg;
    }

    public void setQuantityAvg(double quantityAvg) {
        this.quantityAvg = quantityAvg;
    }

    public double getDiff() {
        return diff;
    }

    public void setDiff(double diff) {
        this.diff = diff;
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getHighest() {
        return highest;
    }

    public void setHighest(double highest) {
        this.highest = highest;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    public double getLowest() {
        return lowest;
    }

    public void setLowest(double lowest) {
        this.lowest = lowest;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return date + "\t" + start + "\t" + highest + "\t" + end + "\t" + lowest + "\t" + quantity + "\t" + money;
    }

    public String calculateStr() {
        DecimalFormat df =  new DecimalFormat("########.#####");
        return toString()
                + "\t" + df.format(c1)//7
                + "\t" + df.format(c2)//8
                + "\t" + df.format(c3)//9
                + "\t" +df.format( c4)//10
                + "\t" + df.format(c5)//11
                + "\t" + df.format(c6)//12
                + "\t" + df.format(c7)//13
                + "\t" + df.format(c8)//14
                + "\t" + df.format(c9)//15
                + "\t" + df.format(c10)//16
                + "\t" + df.format(c11)//17
                + "\t" + df.format(c12)//18
                + "\t" + df.format(c13)//19
                + "\t" + df.format(c14)//20
                + "\t" + df.format(c15)//21
                + "\t" + df.format(c16)//22
                + "\t" + df.format(c17)//23
                ;
    }
}