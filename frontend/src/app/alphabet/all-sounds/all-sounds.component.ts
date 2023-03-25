import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FilesResponse } from '../dto/files-response';
import { SoundService } from '../sound.service';

@Component({
  selector: 'app-all-sounds',
  templateUrl: './all-sounds.component.html',
  styleUrls: ['./all-sounds.component.css']
})
export class AllSoundsComponent implements OnInit {
  @Output("choice") filename: EventEmitter<String> = new EventEmitter<String>();
  filenames: FilesResponse = {files: []};
  isError: boolean = false;
  errorMsg: String = "";
  page: number = 1;

  constructor(private modService: SoundService) {
   }

  ngOnInit(): void {
      this.loadSounds(this.page);
  }

  loadSounds(page: number) {
    this.modService.getSounds(page).subscribe({
      next: (response: FilesResponse) => this.onResponse(response),
      error: (error: HttpErrorResponse) => this.onError(error)
    });
  }
  onResponse(response: FilesResponse): void {
    this.filenames = response;
  }

  onError(error: HttpErrorResponse): void {
    this.isError = true;
    this.errorMsg = error.error.message;
  }

  nextPage(): void {
    this.loadSounds(++this.page);

  }

  prevPage(): void {
    this.loadSounds(--this.page);
  }

  chooseFile(name: String): void {
    this.filename.emit(name);
  }
}
