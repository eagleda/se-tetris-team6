package tetris.domain.model;

/**
 * 입력 상태를 기록/소비하는 헬퍼.
 * 지속 입력(좌/우/소프트드랍)과 1회성 입력(회전/하드드랍/홀드)을 구분합니다.
 */
public final class InputState {
    private boolean left;
    private boolean right;
    private boolean softDrop;
    private boolean rotateCW;
    private boolean rotateCCW;
    private boolean hardDrop;
    private boolean hold;

    public void setLeft(boolean value) {
        left = value;
    }

    public void setRight(boolean value) {
        right = value;
    }

    public void setSoftDrop(boolean value) {
        softDrop = value;
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return right;
    }

    public boolean isSoftDrop() {
        return softDrop;
    }

    public void pressRotateCW() {
        rotateCW = true;
    }

    public void pressRotateCCW() {
        rotateCCW = true;
    }

    public void pressHardDrop() {
        hardDrop = true;
    }

    public void pressHold() {
        hold = true;
    }

    public boolean popRotateCW() {
        boolean temp = rotateCW;
        rotateCW = false;
        return temp;
    }

    public boolean popRotateCCW() {
        boolean temp = rotateCCW;
        rotateCCW = false;
        return temp;
    }

    public boolean popHardDrop() {
        boolean temp = hardDrop;
        hardDrop = false;
        return temp;
    }

    public boolean popHold() {
        boolean temp = hold;
        hold = false;
        return temp;
    }

    public void clearOneShotInputs() {
        rotateCW = false;
        rotateCCW = false;
        hardDrop = false;
        hold = false;
    }
}
