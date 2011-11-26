package interpreter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Input extends Statement {
    private Variable var;

    public Input(Reference ref) {
        var = ref.getVar();
    }

    @Override
    public void execute() {
        try {
            BufferedReader input;
            String str;

            input = new BufferedReader(new InputStreamReader(System.in));
            str = input.readLine();
            switch (var.getType()) {
            case Expression.BOOL:
                var.set(Boolean.valueOf(str));
                break;
            case Expression.INT:
                var.set(new Integer(str));
                break;
            case Expression.STR:
                var.set(str);
                break;
            default:
                throw new RuntimeException("unknown type: " + var.getType());
            }
        } catch (Exception e) {
            System.out.println("input failed: " + e);
            System.exit(1);
        }
    }
}
