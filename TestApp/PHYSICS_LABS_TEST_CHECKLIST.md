# Physics Interactive Lab Test Checklist

Audit date: 2026-10-24

## Test evidence

- The Physics menu contains 40 entries, IDs 1–40 map to 40 existing HTML assets, and the built APK packages all 40.
- Browser sweep: all 40 pages loaded with non-empty content and controls. The sweep exercised the minimum and maximum values of 160 range controls, changed 57 selectors and 43 checkboxes, and reported no uncaught JavaScript errors after the fixes.
- Reset controls were invoked on all 40 pages. A start/run/release action was invoked on each of the 29 pages with a recognized primary action.
- Reference calculations and invalid-input handling were checked for the five formerly empty labs and the updated phase-change model. The other pre-existing labs received route/render/control smoke checks, not independent numerical reference-case verification.
- Real-device checks were blocked: ADB reported no connected devices when installation was attempted. Accessibility on the phone, Android Back behavior, and on-device rendering therefore remain unverified.

## Per-lab checklist

“Route/page”, “input smoke”, and “reset/action” refer to the automated browser checks above. “Model check” distinguishes reference cases checked in this audit from the existing labs whose numeric models were not independently compared against reference solutions.

| # | Lab | Asset | Route/page | Input smoke | Reset/action | Model check | TECNO Spark 10C |
|---:|---|---|---|---|---|---|---|
| 1 | Projectile Motion | [projectile_motion.html](<./app/src/main/assets/InteractiveLab/Physics Lab/projectile_motion.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 2 | Pendulum | [pendulum.html](<./app/src/main/assets/InteractiveLab/Physics Lab/pendulum.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 3 | Spring-Mass System | [spring-mass_system.html](<./app/src/main/assets/InteractiveLab/Physics Lab/spring-mass_system.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 4 | Newton’s Cradle | [newtons_cradle.html](<./app/src/main/assets/InteractiveLab/Physics Lab/newtons_cradle.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 5 | Inclined Plane | [inclined_plane.html](<./app/src/main/assets/InteractiveLab/Physics Lab/inclined_plane.html>) | PASS | PASS | PASS | Reference cases PASS | Not run—ADB device unavailable |
| 6 | Atwood Machine | [atwood_machine.html](<./app/src/main/assets/InteractiveLab/Physics Lab/atwood_machine.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 7 | Rocket Thrust | [rocket_thrust.html](<./app/src/main/assets/InteractiveLab/Physics Lab/rocket_thrust.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 8 | Orbital Motion | [orbital_motion.html](<./app/src/main/assets/InteractiveLab/Physics Lab/orbital_motion.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 9 | Conservation of Energy | [conservation_of_energy.html](<./app/src/main/assets/InteractiveLab/Physics Lab/conservation_of_energy.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 10 | Collisions (1D/2D) | [collisions_1D_and 2D.html](<./app/src/main/assets/InteractiveLab/Physics Lab/collisions_1D_and 2D.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 11 | Wave on a String | [wave_on_a_spring.html](<./app/src/main/assets/InteractiveLab/Physics Lab/wave_on_a_spring.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 12 | Sound Waves | [sound_waves.html](<./app/src/main/assets/InteractiveLab/Physics Lab/sound_waves.html>) | PASS | PASS | PASS | Extreme-amplitude crash fixed; smoke PASS | Not run—ADB device unavailable |
| 13 | Doppler Effect | [doppler_effect.html](<./app/src/main/assets/InteractiveLab/Physics Lab/doppler_effect.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 14 | Interference (Double Slit) | [interference_double_slit.html](<./app/src/main/assets/InteractiveLab/Physics Lab/interference_double_slit.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 15 | Resonance Tube | [resonance_tube.html](<./app/src/main/assets/InteractiveLab/Physics Lab/resonance_tube.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 16 | Fourier Synthesis | [fourier_synthesis.html](<./app/src/main/assets/InteractiveLab/Physics Lab/fourier_synthesis.html>) | PASS | PASS | PASS | Reference cases PASS | Not run—ADB device unavailable |
| 17 | Ohm’s Law | [ohms_law.html](<./app/src/main/assets/InteractiveLab/Physics Lab/ohms_law.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 18 | RC Circuit | [rc_circuit.html](<./app/src/main/assets/InteractiveLab/Physics Lab/rc_circuit.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 19 | Magnetic Field of a Wire | [magnatic_field_of_a_wire.html](<./app/src/main/assets/InteractiveLab/Physics Lab/magnatic_field_of_a_wire.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 20 | Electric Field Lines | [electric_field_lines.html](<./app/src/main/assets/InteractiveLab/Physics Lab/electric_field_lines.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 21 | Lorentz Force | [lorentz_force.html](<./app/src/main/assets/InteractiveLab/Physics Lab/lorentz_force.html>) | PASS | PASS | PASS | Reference cases PASS | Not run—ADB device unavailable |
| 22 | Faraday’s Law | [faradays_law.html](<./app/src/main/assets/InteractiveLab/Physics Lab/faradays_law.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 23 | AC Circuits | [ac_circuits.html](<./app/src/main/assets/InteractiveLab/Physics Lab/ac_circuits.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 24 | Motor/Generator | [motor-generator.html](<./app/src/main/assets/InteractiveLab/Physics Lab/motor-generator.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 25 | Snell’s Law | [snells_law.html](<./app/src/main/assets/InteractiveLab/Physics Lab/snells_law.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 26 | Lens Ray Diagram | [lens-ray_diagram.html](<./app/src/main/assets/InteractiveLab/Physics Lab/lens-ray_diagram.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 27 | Mirror Ray Diagram | [mirror-ray_diagram.html](<./app/src/main/assets/InteractiveLab/Physics Lab/mirror-ray_diagram.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 28 | Prism Dispersion | [prism-dispersion.html](<./app/src/main/assets/InteractiveLab/Physics Lab/prism-dispersion.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 29 | Diffraction Grating | [diffraction_grating.html](<./app/src/main/assets/InteractiveLab/Physics Lab/diffraction_grating.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 30 | Telescope Simulator | [telescope-simulator.html](<./app/src/main/assets/InteractiveLab/Physics Lab/telescope-simulator.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 31 | Ideal Gas Law | [ideal-gas_law.html](<./app/src/main/assets/InteractiveLab/Physics Lab/ideal-gas_law.html>) | PASS | PASS | PASS | Reference cases PASS | Not run—ADB device unavailable |
| 32 | Heat Engine (Carnot) | [heat-engine_or_carnot.html](<./app/src/main/assets/InteractiveLab/Physics Lab/heat-engine_or_carnot.html>) | PASS | PASS | PASS | Reference cases + invalid temperatures PASS | Not run—ADB device unavailable |
| 33 | Specific Heat Capacity | [specific-heat_capacity.html](<./app/src/main/assets/InteractiveLab/Physics Lab/specific-heat_capacity.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 34 | Thermal Expansion | [thermal-expansion.html](<./app/src/main/assets/InteractiveLab/Physics Lab/thermal-expansion.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 35 | Phase Changes | [phase-changes.html](<./app/src/main/assets/InteractiveLab/Physics Lab/phase-changes.html>) | PASS | PASS | PASS | Heating/latent-heat cases PASS | Not run—ADB device unavailable |
| 36 | Photoelectric Effect | [photoelectric-effect.html](<./app/src/main/assets/InteractiveLab/Physics Lab/photoelectric-effect.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 37 | Bohr Model (Hydrogen) | [bohr-model-hydrogen.html](<./app/src/main/assets/InteractiveLab/Physics Lab/bohr-model-hydrogen.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 38 | Radioactive Decay | [radioactive-decay.html](<./app/src/main/assets/InteractiveLab/Physics Lab/radioactive-decay.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 39 | Nuclear Fusion | [nuclear-fusion.html](<./app/src/main/assets/InteractiveLab/Physics Lab/nuclear-fusion.html>) | PASS | PASS | PASS | Smoke only | Not run—ADB device unavailable |
| 40 | Relativity | [relativity.html](<./app/src/main/assets/InteractiveLab/Physics Lab/relativity.html>) | PASS | PASS | PASS | Launch render crash fixed; smoke PASS | Not run—ADB device unavailable |

## Reference checks performed

- Fourier sine at amplitude 1 and frequency 1 Hz: RMS 0.707, peak 1.000, period 1.00 s; square-wave synthesis updated with three harmonics.
- Carnot cycle at Th = 800 K, Tc = 300 K, Qh = 2000 J: efficiency 62.5%, work 1250 J, rejected heat 750 J, entropy transfer 2.500 J/K. Th ≤ Tc displays validation feedback.
- Ideal gas at n = 1 mol, T = 298 K, V = 24.6 L: P = 100.72 kPa; doubling T doubles calculated P.
- Incline at m = 10 kg and θ = 25°: N = 88.91 N and sliding acceleration = 1.479 m/s²; at 20° the selected static-friction model holds the block.
- Lorentz case at q = 1 nC, v = 1000 m/s, B = 0.5 mT, θ = 90°, m = 1 u: |F| = 5.000 × 10⁻¹⁰ N, r = 3.321 × 10⁻¹² m, T = 2.087 × 10⁻¹⁴ s. Reversing q reverses force direction; B = 0 yields zero force without a crash.
- Water phase model: 500 kJ/kg gives liquid water at 29.6 °C; 1 MJ/kg lies on the 100 °C boiling plateau. Presets and Reset were checked.

## Remaining verification

Install the current APK and repeat the screen, touch-control, Back-navigation, and accessibility checks on Android 12 when the TECNO is available to ADB. Independently verify reference calculations for the 33 existing labs marked “Smoke only”; this browser sweep did not constitute a full scientific audit of those pre-existing models.
