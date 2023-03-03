import { FormControl } from "@angular/forms";

export interface CourseForm {
    name: FormControl<String>;
    description: FormControl<String>;
    difficulty: FormControl<String>;
}