import { FormControl } from "@angular/forms"

export interface ExerciseForm {
    letter: FormControl<String>;
    wrongAnswer: FormControl<String>;
    correctAnswer: FormControl<String>;
    courseId: FormControl<Number>;
}