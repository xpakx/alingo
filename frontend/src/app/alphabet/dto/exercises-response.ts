import { Exercise } from "./exercise";

export interface ExercisesResponse {
    page: number,
    size: number,
    totalSize: number,
    exercises: Exercise[]
}