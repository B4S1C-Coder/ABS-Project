package com.b4s1ccoder.play_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b4s1ccoder.common.dto.ManifestUrlDTO;
import com.b4s1ccoder.play_service.service.PlayService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/play")
@RequiredArgsConstructor
public class PlayController {
  private final PlayService playService;

  // This is a dummy route
  @GetMapping("/video/{videoId}")
  public ResponseEntity<ManifestUrlDTO> getMainifestUrl(@PathVariable String videoId) {
    String manifestUrl = playService.getManifestUrl(videoId);
    return ResponseEntity.status(HttpStatus.OK).body(
      ManifestUrlDTO.builder().url(manifestUrl).build()
    );
  }
}
