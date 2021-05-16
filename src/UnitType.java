enum UnitType {
    Bomber(0),
    Bomb(1),
    ITEM(2);

    protected int value;

    UnitType(int value) {
        this.value = value;
    }
}
