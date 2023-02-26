import { FormControl } from "@angular/forms";

export interface RegistrationForm {
    username: FormControl<String>;
    password: FormControl<String>;
    passwordRe: FormControl<String>;
}