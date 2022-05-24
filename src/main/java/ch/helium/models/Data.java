package ch.helium.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Data {
    private float total;
    private float sum;
    private float stddev;
    private float min;
    private float median;
    private float max;
    private float avg;
}
