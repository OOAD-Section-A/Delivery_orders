// validator/DeliveryValidator.java
package validator;

public class DeliveryValidator {

    public boolean validateAddress(String address) {
        return address != null && !address.isEmpty();
    }
}